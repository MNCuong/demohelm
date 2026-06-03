package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatusCardDTO {
    private String code;
    private String label;
    private String color;   // blue, yellow, green, orange, red
    private Long count;
}
