package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatisticMessageSummaryDto {
    private long totalIn;
    private long totalOut;

    public long getTotalAll() {
        return totalIn + totalOut;
    }
}
