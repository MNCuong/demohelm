package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.ReGtxEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReGtxRepository extends JpaRepository<ReGtxEntity,Long> {
    List<ReGtxEntity> findByParticipantId(Long participantId);

    List<ReGtxEntity> findByVsdcode(String vsdcode);
}
