package com.example.web_monitor.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RealtimeDashboardDto {

    private StatisticMessageSummaryDto summary;
    private List<SystemStatusDto> coreComponents;
    private List<SystemAlertDto> recentAlerts;
}
