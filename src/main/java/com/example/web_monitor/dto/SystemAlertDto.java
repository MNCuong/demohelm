package com.example.web_monitor.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Builder
@Data
public class SystemAlertDto {

    private String serviceName;
    private String severity;
    private String message;
    private LocalDateTime time;
    private Double usedValue;
    private Double totalValue;
    private Double usedPercent;

    public SystemAlertDto(String serviceName,String severity, String message, LocalDateTime time,  Double usedValue,
     Double totalValue, Double usedPercent) {
        this.serviceName = serviceName;
        this.severity = severity;
        this.message = message;
        this.time = time;
        this.usedValue = usedValue;
        this.totalValue = totalValue;
        this.usedPercent = usedPercent;
    }

}
