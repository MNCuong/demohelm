package com.example.web_monitor.service;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.RegReportEntity;
import com.example.web_monitor.repository.RegReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RegReportSerivce {
    private final RegReportRepository regReportRepository;

    public RegReportSerivce(RegReportRepository regReportRepository) {
        this.regReportRepository = regReportRepository;
    }

    @Transactional
    public void saveAll(List<RegReportEntity> regReportEntity) {
        regReportRepository.saveAll(regReportEntity);
    }

    @Transactional
    public void updateVsdCode(ParticipantEntity participantEntity, ParticipantDto participantDto) {
        List<RegReportEntity> regReportEntityList = regReportRepository.findByVsdcode(participantEntity.getVsdcode());
        if (regReportEntityList == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        for (RegReportEntity regReportEntity : regReportEntityList) {
            regReportEntity.setVsdcode(participantDto.getVsdcode());
            regReportEntity.setParticipant(participantEntity);
        }
        regReportRepository.saveAll(regReportEntityList);
    }
}
