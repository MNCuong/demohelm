package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@NoArgsConstructor
@Table(name = "TXMSGLOG_DETAIL_HIST")
public class TxMsgLogDetailHistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "AUTOID")
    private Long autoId;

    @Column(name = "ORGSEQID", length = 50)
    private String orgSeqId;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "TXDATE")
    private LocalDateTime txDate;
}


