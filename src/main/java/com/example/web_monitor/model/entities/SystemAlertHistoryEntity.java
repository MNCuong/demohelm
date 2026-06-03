package com.example.web_monitor.model.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_health_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAlertHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name")
    private String serviceName;   // ROUTE, IG, DATABASE

    private String status;        // UP, DOWN, UNKNOWN

    private String severity;      // LOW, MEDIUM, HIGH

    @Column(length = 1000)
    private String message;
    @Column(name = "used_value")
    private Double usedValue;

    @Column(name = "total_value")
    private Double totalValue;

    @Column(name = "used_percent")
    private Double usedPercent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
