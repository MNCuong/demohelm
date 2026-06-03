package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "REGUSERS")
@NoArgsConstructor
@AllArgsConstructor
public class RegUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "AUTOID")
    private Long id;

    @Size(max = 3, message = "VSDCODE tối đa 3 ký tự")
    @Column(name = "VSDCODE", length = 3)
    private String vsdcode;

    @Size(max = 100)
    @Column(name = "USRNAME", length = 100)
    private String usrname;

    @Column(name = "AUTHMODE")
    private String authmode;

    @Size(max = 100)
    @Column(name = "USRPWD", length = 100)
    private String usrpwd;

    @Size(max = 100)
    @Column(name = "TOKENID", length = 100)
    private String tokenid;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "ROLE")
    private Boolean role;

    @Column(name = "USERID")
    private Long userid;

    @Column(name = "UPDATETIME")
    private LocalDateTime updatetime;

    @Size(max = 5)
    @Column(name = "GRID", length = 5)
    private String grid;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTICIPANT_ID", referencedColumnName = "ID", insertable = true, updatable = true)
    private ParticipantEntity participant;
}