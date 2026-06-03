package com.example.web_monitor.dto;

import lombok.Data;

@Data
public class MemberDto {
    private String vsdCode;
    private String shortname;
    private String fullName;

    public MemberDto(String vsdCode, String fullName, String shortname) {
        this.vsdCode = vsdCode;
        this.shortname = shortname;
        this.fullName = fullName;
    }
}
