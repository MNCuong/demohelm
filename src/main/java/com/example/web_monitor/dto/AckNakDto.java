package com.example.web_monitor.dto;

import lombok.Data;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AckNakDto {
    private Long autoid;
    private String seqid;
    private String fullnameen;
    private String shortname;
    private String biccode;
    private String status;
    private String msg;
    private String parentmsg;
    private LocalDateTime actiontime;
    private LocalDateTime updatetime;
    private String corerefid;

    @Data
    @AllArgsConstructor
    public static class Detail {
        private Long autoid;
        private String seqid;
        private String biccode;
        private String status;
        private String msg;
        private String parentmsg;
        private LocalDateTime actiontime;
        private LocalDateTime updatetime;
        private String corerefid;
    }

}

