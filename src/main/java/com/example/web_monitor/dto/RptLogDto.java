package com.example.web_monitor.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RptLogDto {

    private Long autoid;

    private String corerefid;

    private String vsdcode;

    private String status;

    private String filename;

    private String parrentmsg;

    private String systype;

    private String errmsg;

    private LocalDateTime createdate;

    private Long resend;

    private String msgtype;

    private String rptname;
}
