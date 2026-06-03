package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.RegReportEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegReportRepository extends JpaRepository<RegReportEntity, Long> {
    List<RegReportEntity> findByParticipantId(Long participantId);

    List<RegReportEntity> findByVsdcode(String vsdcode);
}
