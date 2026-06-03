package com.example.web_monitor.service;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.RegTerminalEntity;
import com.example.web_monitor.repository.RegTerminalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegTerminalService {
    private final RegTerminalRepository regTerminalRepository;

    public RegTerminalService(RegTerminalRepository regTerminalRepository) {
        this.regTerminalRepository = regTerminalRepository;
    }

    @Transactional
    public void save(RegTerminalEntity regTerminalEntity) {
        regTerminalRepository.save(regTerminalEntity);
    }

    @Transactional
    public void updateVsdCode(ParticipantEntity participantEntity, ParticipantDto participantDto) {
        RegTerminalEntity regTerminalEntity = regTerminalRepository.findByVsdcode(participantEntity.getVsdcode());
        if (regTerminalEntity == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        regTerminalEntity.setVsdcode(participantDto.getVsdcode());
//        regTerminalEntity.setParticipant(participantEntity);
        regTerminalRepository.save(regTerminalEntity);
    }
}
