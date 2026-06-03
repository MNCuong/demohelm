package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "SIGNALS")
public class SignalEntity {
    @Id
    @Column(name = "AUTOID", nullable = false)
    private Long autoId;

    @Column(name = "ORGSEQID", length = 30)
    private String orgSeqId;

    @Lob
    @Column(name = "MSG")
    private String msg;

    @Column(name = "ACTIONTIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date actionTime;

    @Column(name = "QUEUENAME", length = 100)
    private String queueName;

    @Column(name = "STATUS", length = 5)
    private String status;

    @Column(name = "STPREFID", length = 30)
    private String stpRefId;

    @Column(name = "BICCODE", length = 8)
    private String bicCode;

    @Column(name = "MSGTYPE", length = 3)
    private String msgType;

    @Column(name = "REFRECEIVER", length = 8)
    private String refReceiver;
}

