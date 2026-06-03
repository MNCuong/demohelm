package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.ActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionRepository extends JpaRepository<ActionEntity, Long> {
}