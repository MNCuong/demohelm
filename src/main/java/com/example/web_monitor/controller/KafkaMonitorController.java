package com.example.web_monitor.controller;

import com.example.web_monitor.service.KafkaMonitorService;
import com.example.web_monitor.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/kafka")
public class KafkaMonitorController {

    @Autowired
    private KafkaMonitorService kafkaMonitorService;
    @Autowired
    private MessageService messageService;

    @GetMapping("/count/{topic}")
    public ResponseEntity<Map<String, Object>> getTopicCount(@PathVariable String topic) {
        Long count = kafkaMonitorService.getMessageCount(topic);

        Map<String, Object> response = new HashMap<>();
        response.put("topic", topic);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/throughput/{topic}")
    public ResponseEntity<Map<String, Object>> getTopicThroughput(@PathVariable String topic) {
        Double throughput = kafkaMonitorService.getThroughput(topic);

        Map<String, Object> response = new HashMap<>();
        response.put("topic", topic);
        response.put("throughput", throughput);
        log.info("Throughput API return: {}", throughput);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/avg-latency")
    public ResponseEntity<Double> getAvgLatency() {
        double avg = kafkaMonitorService.getAvgLatency();
        log.info("AvgLatency API return: {}", avg);
        return ResponseEntity.ok(avg);
    }

    @GetMapping("/error-rate")
    public ResponseEntity<?> getErrorRate(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) date = LocalDate.now();
        int countErrorDB = messageService.getError(date);
        long countErrorKafka = kafkaMonitorService.getErrorCount("tcpserver.error", date);
        return ResponseEntity.ok(countErrorDB + countErrorKafka);
    }
}