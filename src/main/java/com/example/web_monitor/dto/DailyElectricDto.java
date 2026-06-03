package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class DailyElectricDto {
    private LocalDate day;
    private Long totalElectric;
}
