package com.example.web_monitor.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "DEFSCHEMA")
public class DefschemaEntity {
    @Id
    @Column(name = "AUTOID")
    private Long autoid;

    @Size(max = 3)
    @Column(name = "SCHTYPE", length = 3)
    private String schtype;

    @Size(max = 50)
    @Column(name = "SCHNAME", length = 50)
    private String schname;

    @Size(max = 3500)
    @Column(name = "SCHBODY", length = 3500)
    private String schbody;

    @Size(max = 100)
    @Column(name = "NOTES", length = 100)
    private String notes;

    @Column(name = "USERID")
    private Long userid;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "UPDATETIME")
    private Instant updatetime;

}