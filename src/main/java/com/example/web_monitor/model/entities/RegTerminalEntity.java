package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "REGTERMINAL")
@NoArgsConstructor
@AllArgsConstructor
public class RegTerminalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "regterminal_seq2")
    @SequenceGenerator(name = "regterminal_seq2", sequenceName = "REGTERMINAL_SEQ2", allocationSize = 1)
    @Column(name = "AUTOID")
    private Long autoid;


    @Size(max = 3, message = "VSDCODE tối đa 3 ký tự")
    @Column(name = "VSDCODE", length = 3)
    private String vsdcode;

    @Size(max = 30)
    @Column(name = "IPADDR", length = 30)
    private String ipaddr;

    @Size(max = 30)
    @Column(name = "MACADDR", length = 30)
    private String macaddr;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "USERID")
    private Long userid;

    @Column(name = "UPDATETIME")
    private LocalDateTime updatetime;

    @Column(name = "LOGINTIME")
    private LocalDate logintime;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JoinColumn(name = "PARTICIPANT_ID", referencedColumnName = "ID", insertable = true, updatable = true)
    private ParticipantEntity participant;



}