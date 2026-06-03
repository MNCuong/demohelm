package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@AllArgsConstructor
@Builder
@Data
public class ParsedKafkaMessageDto {
    private String payload;
    private String senderBic;
    private LocalDate tradeDate;
    private String tag20;
    private String tradeDateError;
}
