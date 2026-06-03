package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@Table(name = "STATICDATA")
@NoArgsConstructor
@AllArgsConstructor
public class StaticDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;
    @Size(max = 50)
    @Column(name = "DATACODE", length = 50)
    private String datacode;

    @Size(max = 50)
    @Column(name = "DATAVAL", length = 50)
    private String dataval;

    @Size(max = 500)
    @Column(name = "DATACAP", length = 500)
    private String datacap;


    public StaticDataEntity(String datacode, String biccode, String vsdcode) {
        this.datacode=datacode;
        this.dataval=biccode;
        this.datacap=vsdcode;
    }
}