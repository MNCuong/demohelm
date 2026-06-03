package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.RegGroupUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegGroupUserRepository extends JpaRepository<RegGroupUserEntity, String> {
}
