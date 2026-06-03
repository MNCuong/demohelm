package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.model.entities.ParticipantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParticipantMapper {

    // Chuyển DTO → Entity
    @Mapping(target = "vsdcode", ignore = true)            // Nếu muốn Service gán vsdcode tự động
    @Mapping(target = "updatetime", ignore = true)    // Service sẽ gán CURRENT_TIMESTAMP
    ParticipantEntity toEntity(ParticipantDto dto);

    // Chuyển Entity → DTO
    ParticipantDto toDto(ParticipantEntity entity);


}
