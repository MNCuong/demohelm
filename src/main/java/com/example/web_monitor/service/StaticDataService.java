package com.example.web_monitor.service;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.model.entities.ParticipantEntity;
import com.example.web_monitor.model.entities.StaticDataEntity;
import com.example.web_monitor.repository.StaticDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class StaticDataService {
    private final StaticDataRepository staticDataRepository;

    public StaticDataService(StaticDataRepository staticDataRepository) {
        this.staticDataRepository = staticDataRepository;
    }

    @Transactional
    public void saveAll(List<StaticDataEntity> staticDataList) {
        staticDataRepository.saveAll(staticDataList);
    }

    @Transactional
    public void updateDataCapAndDataVal(ParticipantEntity participantEntity, ParticipantDto participantDto) {
        // Cập nhật vsdcode
        if (!Objects.equals(participantEntity.getVsdcode(), participantDto.getVsdcode())) {
            staticDataRepository.updateDatacap(participantEntity.getVsdcode(), participantDto.getVsdcode());
            staticDataRepository.updateDataval(participantEntity.getVsdcode(), participantDto.getVsdcode());
        }

        // Cập nhật biccode
        if (!Objects.equals(participantEntity.getBiccode(), participantDto.getBiccode())) {
            staticDataRepository.updateDatacap(participantEntity.getBiccode(), participantDto.getBiccode());
            staticDataRepository.updateDataval(participantEntity.getBiccode(), participantDto.getBiccode());
        }
    }

    @Transactional
    public void deleteByVsdCode(String vsdcode, String biccode) {
        staticDataRepository.deleteByVsdCodeOrBicCode(vsdcode, biccode);
    }
}
