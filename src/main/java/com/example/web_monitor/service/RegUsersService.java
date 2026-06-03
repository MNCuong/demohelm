package com.example.web_monitor.service;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.RegUserEntity;
import com.example.web_monitor.repository.RegUsersRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class RegUsersService {
    private final RegUsersRepository regUsersRepository;

    public RegUsersService(RegUsersRepository regUsersRepository) {
        this.regUsersRepository = regUsersRepository;
    }

    @Transactional
    public void save(RegUserEntity regUserEntity) {
        regUsersRepository.save(regUserEntity);
    }


    @Transactional
    public void updateVsdCode(ParticipantEntity participantEntity, ParticipantDto participantDto) {
        RegUserEntity regUserEntity = regUsersRepository.findByVsdcode(participantEntity.getVsdcode());
        if (regUserEntity == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }
        regUserEntity.setVsdcode(participantDto.getVsdcode());
        regUserEntity.setGrid(participantDto.getGroupqueue());
        regUserEntity.setParticipant(participantEntity);
        regUsersRepository.save(regUserEntity);
    }
    public String findBiccode(String senderBicUsername){
        return regUsersRepository.findBiccode(senderBicUsername);
    }
    public List<String> findUsrname(){
        return regUsersRepository.findAllUsrname();
    }
}
