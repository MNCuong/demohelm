package com.example.web_monitor.service;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.ReGtxEntity;
import com.example.web_monitor.repository.ReGtxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReGtxService {
    private final ReGtxRepository reGtxRepository;

    public ReGtxService(ReGtxRepository reGtxRepository) {
        this.reGtxRepository = reGtxRepository;
    }

    @Transactional
    public void saveAll(List<ReGtxEntity> reGtxList) {
        reGtxRepository.saveAll(reGtxList);
    }

    @Transactional
    public void updateVsdCode(ParticipantEntity participantEntity, ParticipantDto participantDto) {
        List<ReGtxEntity> reGtxEntityList = reGtxRepository.findByVsdcode(participantEntity.getVsdcode());
        if (reGtxEntityList == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        for (ReGtxEntity reGtxEntity : reGtxEntityList) {
            reGtxEntity.setVsdcode(participantDto.getVsdcode());
            reGtxEntity.setParticipant(participantEntity);
        }
        reGtxRepository.saveAll(reGtxEntityList);

    }
}
