package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "SENTACKNAK")
public class AckNakEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUTOID")
    private Long autoid;

    @Size(max = 35)
    @Column(name = "SEQID", length = 35)
    private String seqid;

    @Size(max = 20)
    @Column(name = "BICCODE", length = 20)
    private String biccode;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ACTIONTIME")
    private LocalDateTime actiontime;

    @Column(name = "UPDATETIME")
    private LocalDateTime updatetime;

    @Size(max = 20)
    @Column(name = "COREREFID", length = 20)
    private String corerefid;

    @Lob
    @Column(name = "MSG")
    private String msg;

    @Lob
    @Column(name = "PARENTMSG")
    private String parentmsg;


}