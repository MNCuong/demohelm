package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.SignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignalRepository extends JpaRepository<SignalEntity, Long> {
    List<SignalEntity> findByOrgSeqIdOrStpRefIdOrderByActionTimeAsc(String orgSeqId, String stpRefId);
}

