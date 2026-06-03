package com.example.web_monitor.service;

import com.example.web_monitor.dto.SystemAlertDto;
import com.example.web_monitor.model.entities.SystemAlertHistoryEntity;
import com.example.web_monitor.repository.SystemAlertHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertHistoryService {

    private final SystemAlertHistoryRepository systemHealthHistoryRepository;

    @Async
    @Transactional
    public void save(
            String serviceName,
            String status,
            String severity,
            String message,
            Double usedValue,
            Double totalValue,
            Double usedPercent
    ) {

        SystemAlertHistoryEntity systemAlertHistoryEntity =
                SystemAlertHistoryEntity.builder()
                        .serviceName(serviceName)
                        .status(status)
                        .severity(severity)
                        .message(message)
                        .usedValue(usedValue)
                        .totalValue(totalValue)
                        .usedPercent(usedPercent)
                        .createdAt(LocalDateTime.now())
                        .build();


        systemHealthHistoryRepository.save(systemAlertHistoryEntity);
    }
    public void save(
            String serviceName,
            String status,
            String severity,
            String message
    ) {
        save(serviceName, status, severity, message, null, null, null);
    }

    //    public List<SystemAlertDto> getRecentAlerts() {
//        return systemHealthHistoryRepository.findAll()
//                .stream()
//                .map(a -> new SystemAlertDto(
//                        a.getServiceName(),
//                        Severity.valueOf(a.getSeverity()),
//                        a.getMessage(),
//                        a.getCreatedAt()
//                ))
//                .toList();    }
    public List<SystemAlertDto> getRecentAlerts() {

        return systemHealthHistoryRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(e -> SystemAlertDto.builder()
                        .serviceName(e.getServiceName())
                        .severity(e.getSeverity())
                        .message(e.getMessage())
                        .time(e.getCreatedAt())
                        .usedValue(e.getUsedValue())
                        .totalValue(e.getTotalValue())
                        .usedPercent(e.getUsedPercent())
                        .build())
                .toList();
    }


    public void deleteAll() {
        systemHealthHistoryRepository.deleteAll();
    }
}
