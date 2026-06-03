package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SENTMSG")
public class SentMsgEntity {
    @Id
    @Column(name = "AUTOID", nullable = false)
    private Long autoId;

    @Column(name = "SEQID", length = 30)
    private String seqId;

    @Column(name = "BICCODE", length = 20)
    private String bicCode;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "ACTIONTIME")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime actionTime;

    @Column(name = "UPDATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updateTime;

    @Column(name = "COREREFID", length = 50)
    private String coreRefId;

    @Column(name = "EXTENSION", length = 200)
    private String extension;

    @Column(name = "TRFCODE", length = 200)
    private String trfCode;

    @Lob
    @Column(name = "MSG")
    private String msg;

    @Lob
    @Column(name = "ACKMSG")
    private String ackMsg;

    @Column(name = "REFSENDER", length = 20)
    private String refSender;

    @Column(name = "STPREFID", length = 50)
    private String stpRefId;
}

