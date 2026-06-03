package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@Entity
@Table(name = "PARTICIPANTS")
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "VSDCODE", length = 3)
    private String vsdcode;

    @Column(name = "BICCODE", length = 20)
    private String biccode;

    @Column(name = "SHORTNAME", length = 50)
    private String shortname;

    @Column(name = "FULLNAMEEN", length = 100)
    private String fullnameen;

    @Column(name = "FULLNAMEVN", length = 100)
    private String fullnamevn;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "GROUPID")
    private Long groupid;

    @Column(name = "USERID")
    private Long userid;

    @Column(name = "UPDATETIME")
    private LocalDateTime updatetime;

    @ColumnDefault("1")
    @Column(name = "CONSUMERS")
    private Integer consumers;

    @Column(name = "GROUPQUEUE", length = 5)
    private String groupqueue;

    @ColumnDefault("('M')")
    @Column(name = "USERTYPE", length = 5)
    private String usertype;

    // Quan hệ với RegTx
    @OneToMany(mappedBy = "participant",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReGtxEntity> regTxList = new ArrayList<>();

    // Quan hệ với RegReport
    @OneToMany(mappedBy = "participant",fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RegReportEntity> regReportList = new ArrayList<>();

    // Quan hệ với RegTerminal
    @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private RegTerminalEntity regTerminal;
    // Quan hệ với RegUser
    @OneToOne(mappedBy = "participant", cascade = CascadeType.ALL, orphanRemoval = true)
    private RegUserEntity regUser;

    @Transient
    public String getFullName() {
        return vsdcode + " - " + shortname;
    }
}