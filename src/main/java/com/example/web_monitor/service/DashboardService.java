package com.example.web_monitor.service;

import com.example.web_monitor.dto.GatewayHealth;
import com.example.web_monitor.dto.SystemStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class DashboardService {

    private final String hostHealCheckRoute;
    private final String hostHealCheckIg;
    private final MessageSource messageSource;
    private final HealthcheckAsyncService healthcheckAsyncService;
    private final AlertHistoryService alertHistoryService;

    // Các hằng số cho ngưỡng cảnh báo
    private static final double DISK_USAGE_CRITICAL_THRESHOLD = 90.0;
    private static final double DISK_USAGE_WARNING_THRESHOLD = 70.0;
    private static final double DB_USAGE_CRITICAL_THRESHOLD = 90.0;
    private static final double DB_USAGE_WARNING_THRESHOLD = 70.0;

    public DashboardService(
            @Value("${healthcheck.host.route}") String hostHealCheckRoute,
            @Value("${healthcheck.host.ig}") String hostHealCheckIg,
            MessageSource messageSource,
            HealthcheckAsyncService healthcheckAsyncService,
            AlertHistoryService alertHistoryService
    ) {
        this.hostHealCheckRoute = hostHealCheckRoute;
        this.hostHealCheckIg = hostHealCheckIg;
        this.messageSource = messageSource;
        this.healthcheckAsyncService = healthcheckAsyncService;
        this.alertHistoryService = alertHistoryService;
    }

    public GatewayHealth fetchGatewayHealth() {
        CompletableFuture<Map<String, Object>> routeFuture =
                healthcheckAsyncService.check("/actuator/health", hostHealCheckRoute);

        CompletableFuture<Map<String, Object>> igFuture =
                healthcheckAsyncService.check("/actuator/health", hostHealCheckIg);

        CompletableFuture.allOf(routeFuture, igFuture).join();

        Map<String, Object> route = routeFuture.join();
        Map<String, Object> ig = igFuture.join();

        handleRoute(route);
        handleIg(ig);

        return new GatewayHealth(route, ig);
    }

    private void handleRoute(Map<String, Object> route) {
        boolean isConnectionError = Boolean.TRUE.equals(route.get("healthcheckError"));
        String routeStatus = route.getOrDefault("status", "UNKNOWN").toString();

        if (isConnectionError) {
            alertHistoryService.save(
                    "ROUTE",
                    "DOWN",
                    "HIGH",
                    msg("alert.healthcheck.unreachable.route"),
                    null, null, null
            );
            return;
        }

        // Chỉ lưu alert nếu ROUTE không UP
        if (!"UP".equals(routeStatus)) {
            String severity = getSeverityForStatus(routeStatus);
            alertHistoryService.save(
                    "ROUTE",
                    routeStatus,
                    severity,
                    msg("alert.route.down"),
                    null, null, null
            );
        }

        // Xử lý các components bên trong ROUTE
        Map<String, Object> components = getMap(route.get("components"));
        handleDatabase(components);
        handleDiskSpace(components, "ROUTE");
    }

    private void handleIg(Map<String, Object> ig) {
        boolean isConnectionError = Boolean.TRUE.equals(ig.get("healthcheckError"));
        String igStatus = ig.getOrDefault("status", "UNKNOWN").toString();

        // Chỉ lưu alert nếu có sự cố
        if (isConnectionError) {
            alertHistoryService.save(
                    "IG",
                    "DOWN",
                    "HIGH",
                    msg("alert.healthcheck.unreachable.ig"),
                    null, null, null
            );
            return;
        }

        // Chỉ lưu alert nếu IG không UP
        if (!"UP".equals(igStatus)) {
            String severity = getSeverityForStatus(igStatus);
            alertHistoryService.save(
                    "IG",
                    igStatus,
                    severity,
                    msg("alert.ig.down"),
                    null, null, null
            );
        }

        // Xử lý các components bên trong IG
        Map<String, Object> components = getMap(ig.get("components"));
        handleDiskSpace(components, "IG");
    }

    private String getSeverityForStatus(String status) {
        if ("DOWN".equalsIgnoreCase(status)) {
            return "HIGH";
        } else if ("OUT_OF_SERVICE".equalsIgnoreCase(status) ||
                "UNSTABLE".equalsIgnoreCase(status)) {
            return "MEDIUM";
        } else {
            return "LOW"; // UNKNOWN hoặc các status khác
        }
    }

    private void handleDatabase(Map<String, Object> components) {
        Map<String, Object> db = getMap(components.get("db"));

        // Nếu không có db trong components
        if (db.isEmpty()) {
            return;
        }

        String dbStatus = db.getOrDefault("status", "UNKNOWN").toString();
        Map<String, Object> details = getMap(db.get("details"));

        Double usedVal = null;
        Double totalVal = null;
        Double percent = null;

        // Lấy thông tin dung lượng database nếu có
        if (!details.isEmpty()) {
            Object usedObj = details.get("used");
            Object totalObj = details.get("total");

            if (usedObj instanceof Number && totalObj instanceof Number) {
                usedVal = ((Number) usedObj).doubleValue();
                totalVal = ((Number) totalObj).doubleValue();

                if (totalVal > 0) {
                    percent = usedVal / totalVal * 100;
                }
            }
        }

        // Chỉ lưu alert nếu database không UP
        if (!"UP".equals(dbStatus)) {
            String severity = getSeverityForStatus(dbStatus);
            String message = msg("alert.database.down");

            alertHistoryService.save(
                    "DATABASE",
                    dbStatus,
                    severity,
                    message,
                    usedVal,
                    totalVal,
                    percent
            );
        }
        // Nếu database UP, chỉ kiểm tra ngưỡng usage
        else {
            checkUsageThreshold("DATABASE", dbStatus, percent, usedVal, totalVal,
                    DB_USAGE_WARNING_THRESHOLD, DB_USAGE_CRITICAL_THRESHOLD);
        }
    }

    private void handleDiskSpace(Map<String, Object> components, String serviceName) {
        Map<String, Object> diskSpace = getMap(components.get("diskSpace"));

        if (!diskSpace.isEmpty()) {
            String diskStatus = diskSpace.getOrDefault("status", "UNKNOWN").toString();
            Map<String, Object> details = getMap(diskSpace.get("details"));

            // Lấy thông tin dung lượng disk
            Double totalBytes = getDoubleValue(details.get("total"));
            Double freeBytes = getDoubleValue(details.get("free"));
            Double usedPercent = null;
            Double usedBytes = null;

            if (totalBytes != null && totalBytes > 0 && freeBytes != null) {
                usedBytes = totalBytes - freeBytes;
                usedPercent = (usedBytes / totalBytes) * 100.0;
                log.info("{} Disk Space - Total: {}, Free: {}, Used: {}, Percent: {}%",
                        serviceName, totalBytes, freeBytes, usedBytes, usedPercent);

                // Chỉ lưu alert nếu disk không UP
                if (!"UP".equals(diskStatus)) {
                    String severity = getSeverityForStatus(diskStatus);
                    String message = msg("alert.diskspace.down", serviceName);
                    String displayName = serviceName.equals("ROUTE") ? "DISK_ROUTE" : "DISK_IG";

                    alertHistoryService.save(
                            displayName,
                            diskStatus,
                            severity,
                            message,
                            usedBytes,
                            totalBytes,
                            usedPercent
                    );
                }
                // Nếu disk UP, chỉ kiểm tra ngưỡng usage
                else {
                    String displayName = serviceName.equals("ROUTE") ? "DISK_ROUTE" : "DISK_IG";
                    checkUsageThreshold(displayName, diskStatus, usedPercent, usedBytes,
                            totalBytes,
                            DISK_USAGE_WARNING_THRESHOLD, DISK_USAGE_CRITICAL_THRESHOLD);
                }
            } else {
                if (!"UP".equals(diskStatus)) {
                    String severity = getSeverityForStatus(diskStatus);
                    String message = msg("alert.diskspace.down", serviceName);
                    String displayName = serviceName.equals("ROUTE") ? "DISK_ROUTE" : "DISK_IG";

                    alertHistoryService.save(
                            displayName,
                            diskStatus,
                            severity,
                            message,
                            null, null, null
                    );
                }
            }
        }
    }

    private void checkUsageThreshold(String componentName, String status,
                                     Double usedPercent, Double usedValue,
                                     Double totalValue,
                                     double warningThreshold, double criticalThreshold) {
        if (!"UP".equals(status) || usedPercent == null) {
            return;
        }

        if (usedPercent >= criticalThreshold) {
            alertHistoryService.save(
                    componentName,
                    "UP",
                    "HIGH",
                    msg("alert.usage.critical",
                            componentName,
                            String.format("%.1f", usedPercent)),
                    usedValue,
                    totalValue,
                    usedPercent
            );
        } else if (usedPercent >= warningThreshold) {
            alertHistoryService.save(
                    componentName,
                    "UP",
                    "MEDIUM",
                    msg("alert.usage.warning",
                            componentName,
                            String.format("%.1f", usedPercent)),
                    usedValue,
                    totalValue,
                    usedPercent
            );
        }
    }
    private Double getDoubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return null;
    }

    private Long getLongValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Object obj) {
        return (obj instanceof Map) ? (Map<String, Object>) obj : Map.of();
    }

    public List<SystemStatusDto> getCoreComponents(GatewayHealth health) {
        Map<String, Object> healthRoute = health.getRoute();
        Map<String, Object> healthIg = health.getIg();

        Map<String, Object> componentsRoute = getMap(healthRoute.get("components"));
        Map<String, Object> componentsIg = getMap(healthIg.get("components"));

        Map<String, Object> db = getMap(componentsRoute.get("db"));

        String dbStatus = getStatus(db, "UNKNOWN");
        String routeStatus = getStatus(healthRoute, "UNKNOWN");
        String igStatus = getStatus(healthIg, "UNKNOWN");

        List<SystemStatusDto> list = new ArrayList<>();
        list.add(new SystemStatusDto("Database", dbStatus));
        list.add(new SystemStatusDto("Route", routeStatus));
        list.add(new SystemStatusDto("IG", igStatus));

        return list;
    }

    private String getStatus(Map<String, Object> component, String defaultValue) {
        if (component == null || component.isEmpty()) {
            return defaultValue;
        }
        return component.getOrDefault("status", defaultValue).toString();
    }

    private String msg(String key, Object... args) {
        return messageSource.getMessage(
                key,
                args,
                LocaleContextHolder.getLocale()
        );
    }
}