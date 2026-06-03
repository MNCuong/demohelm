package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "RPTLOG")
@Getter
@Setter
public class RptlogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rptlog_seq")
    @SequenceGenerator(
            name = "rptlog_seq",
            sequenceName = "SEQ_RPTLOG",
            allocationSize = 1
    )
    @Column(name = "AUTOID")
    private Long autoid;

    @Column(name = "COREREFID", length = 200)
    private String corerefid;

    @Column(name = "VSDCODE", length = 6)
    private String vsdcode;

    @Column(name = "STATUS", length = 1)
    private String status;

    @Column(name = "FILENAME", length = 1000)
    private String filename;

    @Column(name = "PARRENTMSG", length = 2000)
    private String parrentmsg;

    @Column(name = "SYSTYPE", length = 5)
    private String systype;

    @Column(name = "ERRMSG", length = 300)
    private String errmsg;

    @Column(name = "CREATEDATE")
    private LocalDateTime createdate;

    @Column(name = "RESEND")
    private Long resend;

    @Column(name = "MSGTYPE", length = 200)
    private String msgtype;

    @Column(name = "RPTNAME", length = 200)
    private String rptname;
}
