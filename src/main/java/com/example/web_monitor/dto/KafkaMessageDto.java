package com.example.web_monitor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KafkaMessageDto {
    private String key;
    private String value;
    private String headers;
    private LocalDateTime timestamp;
    private long offset;
    public KafkaMessageDto(String key, String value, String headers,LocalDateTime timestamp, Long offset) {
        this.key = key;
        this.value = value;
        this.headers = headers;
        this.timestamp = timestamp;
        this.offset = offset;
    }
}
