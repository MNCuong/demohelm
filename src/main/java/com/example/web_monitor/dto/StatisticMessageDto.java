package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class StatisticMessageDto {

    private String refCode;
    private Long totalIn;
    private Long totalOut;
    private Long total;
}
