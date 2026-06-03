package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.SystemAlertHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemAlertHistoryRepository extends JpaRepository<SystemAlertHistoryEntity, Long> {
    List<SystemAlertHistoryEntity> findAllByOrderByCreatedAtDesc();

}
