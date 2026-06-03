package com.example.web_monitor.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "REGGROUPUSER")
public class RegGroupUserEntity {
    @Id
    @Size(max = 5)
    @Column(name = "GRID", length = 5)
    private String grid;

    @Size(max = 50)
    @Column(name = "GRNAME", length = 50)
    private String grname;

    @Size(max = 50)
    @Column(name = "QUEUENAME", length = 50)
    private String queuename;

    @Size(max = 3)
    @Column(name = "CONSUMERS", length = 3)
    private String consumers;

}