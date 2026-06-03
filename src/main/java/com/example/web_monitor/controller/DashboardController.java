package com.example.web_monitor.controller;

import com.example.web_monitor.dto.DailyElectricDto;
import com.example.web_monitor.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequestMapping("/dashboard")
@RestController
public class DashboardController {
    private final MessageService messageService;

    public DashboardController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/realtime")
    public Map<String, Object> realtime30Minutes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startOfDay,
            @RequestParam(required = false ) Integer minuteParam) {
        int interval = minuteParam != null ? minuteParam : 30;
        LocalDate baseDate = startOfDay != null ? startOfDay : LocalDate.now();

        LocalDateTime start = baseDate.atStartOfDay();
        LocalDateTime end = baseDate.plusDays(1).atStartOfDay();
        return messageService.realtimeByMinute(start,end, interval);
    }


    @GetMapping("/daily")
    @ResponseBody
    public Map<String, Object> daily(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(10);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        List<DailyElectricDto> list =
                messageService.getElectricByDay(
                        startDate.atStartOfDay(),
                        endDate.plusDays(1).atStartOfDay()
                );

        List<String> labels = list.stream()
                .map(d -> d.getDay().toString())
                .toList();

        List<Long> data = list.stream()
                .map(DailyElectricDto::getTotalElectric)
                .toList();

        Map<String, Object> res = new HashMap<>();
        res.put("labels", labels);
        res.put("data", data);

        return res;
    }


}
