package com.example.web_monitor.dto;

import lombok.Data;

@Data
public class SystemStatusDto {

    private String name;
    private String status;

    public SystemStatusDto(String name, String status) {
        this.name = name;
        this.status = status;
    }

}
