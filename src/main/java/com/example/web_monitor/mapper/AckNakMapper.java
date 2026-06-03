package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.AckNakDto;
import com.example.web_monitor.model.entities.AckNakEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AckNakMapper {
    AckNakDto toDto(AckNakEntity entity);

    List<AckNakDto> toDtoList(List<AckNakEntity> entities);

    AckNakEntity toEntity(AckNakDto dto);
}
