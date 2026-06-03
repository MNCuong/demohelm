package com.example.web_monitor.controller;

import com.example.web_monitor.dto.GatewayHealth;
import com.example.web_monitor.dto.RealtimeDashboardDto;
import com.example.web_monitor.repository.SystemAlertHistoryRepository;
import com.example.web_monitor.service.AlertHistoryService;
import com.example.web_monitor.service.DashboardService;
import com.example.web_monitor.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {
    private final MessageService messageService;
    private final DashboardService dashboardService;
    private final AlertHistoryService alertHistoryService;

    public MonitorController(
            MessageService messageService,
            DashboardService dashboardService,
            AlertHistoryService alertHistoryService) {
        this.messageService = messageService;
        this.dashboardService = dashboardService;
        this.alertHistoryService = alertHistoryService;
    }

    @GetMapping("/realtime")
    public RealtimeDashboardDto realtimeDashboard() {
        GatewayHealth health = dashboardService.fetchGatewayHealth();

        return RealtimeDashboardDto.builder()
                .summary(messageService.getTodayTotalStatistic())
                .coreComponents(
                        dashboardService.getCoreComponents(health)
                )
                .recentAlerts(
                        alertHistoryService.getRecentAlerts()
                )
                .build();
    }
    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAllAlerts() {
        alertHistoryService.deleteAll();
        return ResponseEntity.noContent().build();
    }

}
