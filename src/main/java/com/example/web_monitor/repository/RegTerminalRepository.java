package com.example.web_monitor.repository;

import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.RegTerminalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RegTerminalRepository extends JpaRepository<RegTerminalEntity,Long> {
    RegTerminalEntity findByParticipantId(Long participantId);

    RegTerminalEntity findByVsdcode(String vsdcode);
}
