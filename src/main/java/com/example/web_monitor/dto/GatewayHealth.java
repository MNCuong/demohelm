package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class GatewayHealth {
    private Map<String, Object> route;
    private Map<String, Object> ig;
}
