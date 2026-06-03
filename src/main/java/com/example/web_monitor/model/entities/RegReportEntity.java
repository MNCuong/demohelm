package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@Table(name = "REGREPORT")
@NoArgsConstructor
@AllArgsConstructor
public class RegReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 3, message = "VSDCODE tối đa 3 ký tự")
    @Column(name = "VSDCODE", length = 3)
    private String vsdcode;

    @Size(max = 20)
    @Column(name = "EVENTCODE", length = 20)
    private String eventcode;

    @Size(max = 10)
    @Column(name = "RPTCODE", length = 10)
    private String rptcode;

    @Size(max = 2)
    @Column(name = "STATUS", length = 2)
    private String status;
    @ManyToOne
    @JoinColumn(name = "PARTICIPANT_ID", referencedColumnName = "ID", insertable = true, updatable = true)
    private ParticipantEntity participant;


}