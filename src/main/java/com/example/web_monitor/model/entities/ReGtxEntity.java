package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "REGTX")
@NoArgsConstructor
@AllArgsConstructor
public class ReGtxEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "AUTOID")
    private Long autoid;

    @Size(max = 3, message = "VSDCODE tối đa 3 ký tự")
    @Column(name = "VSDCODE", length = 3)
    private String vsdcode;

    @Size(max = 5, message = "MSGTYPE tối đa 5 ký tự")
    @Column(name = "MSGTYPE", length = 5)
    private String msgtype;

    @Size(max = 30)
    @Column(name = "MSGCODE", length = 30)
    private String msgcode;

    @Size(max = 4)
    @Column(name = "TLTXCD", length = 4)
    private String tltxcd;

    @Column(name = "DMAMODE")
    private String dmamode;

    @Column(name = "USERID")
    private Long userid;

    @Column(name = "UPDATETIME")
    private LocalDateTime updatetime;

    @Size(max = 1)
    @Column(name = "AUTOTRANS", length = 1)
    private String autotrans;
    @ManyToOne
    @JoinColumn(name = "PARTICIPANT_ID", referencedColumnName = "ID", insertable = true, updatable = true)
    private ParticipantEntity participant;


}