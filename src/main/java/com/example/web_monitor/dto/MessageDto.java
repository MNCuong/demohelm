package com.example.web_monitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDto {
    private Long autoId;         // AUTOID
    private String trfCode;      // Type (Mã hiệu điện)
    private String refSender;    // Sender
    private String refReceiver;  // Receiver
    private String status;       // Status
    private LocalDateTime txDate;         // Time (TXDATE)
    private LocalDateTime createTst;         // Time (TXDATE)
    private String coreRefId;    // Mã hiệu core
    private String refSeqId;     // (REFSEQID)
    private String orgSeqId;     // (ORGSEQID)
    private String msgBody;     // Nội dung điện
    private String inOutFlag;
    private String seqId;
    private String feedback;
    private String stpRefId;
    private String refCode;
    private String usrName;
}