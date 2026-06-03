package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "TXMSGLOGHIST")
public class MessageHistEntity {
    @Id
    @Column(name = "AUTOID", nullable = false)
    private Long autoId;

    @Column(name = "SEQID", length = 30)
    private String seqId;

    @Column(name = "INOUTFLAG", length = 1)
    private String inOutFlag;

    @Column(name = "ORGSEQID", length = 50)
    private String orgSeqId;

    @Column(name = "REFSEQID", length = 50)
    private String refSeqId;

    @Column(name = "TXDATE")
    private LocalDateTime txDate;

    @Column(name = "CREATETST")
    private LocalDateTime createTst;

    @Column(name = "SENDTST")
    private Timestamp sendTst;

    @Column(name = "REFCODE", length = 30)
    private String refCode;

    @Column(name = "TRFCODE", length = 100)
    private String trfCode;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "ERRCODE")
    private Long errCode;

    @Column(name = "FEEDBACK", length = 250)
    private String feedback;

    @Column(name = "USRNAME", length = 30)
    private String usrName;

    @Column(name = "IPADDRESS", length = 30)
    private String ipAddress;

    @Column(name = "TXNUM", length = 30)
    private String txNum;

    @Column(name = "ERRMSG", length = 1000)
    private String errMsg;

    @Column(name = "PARRENTID", length = 20)
    private String parrentId;

    @Column(name = "REFSENDER", length = 30)
    private String refSender;

    @Column(name = "REFRECEIVER", length = 30)
    private String refReceiver;

    @Column(name = "REFSYMBOL", length = 30)
    private String refSymbol;

    @Column(name = "REFOWNER", length = 30)
    private String refOwner;

    @Column(name = "REFQTTY", length = 50)
    private String refQtty;

    @Column(name = "COREREFID", length = 100)
    private String coreRefId;

    @Column(name = "BANKREFID", length = 20)
    private String bankRefId;

    @Lob
    @Column(name = "MSGCORE")
    private String msgCore;

    @Lob
    @Column(name = "MSGBODY")
    private String msgBody;

    @Column(name = "REFORGSEQID", length = 50)
    private String refOrgSeqId;

    @Column(name = "REFORGSEQID21", length = 50)
    private String refOrgSeqId21;
}
