package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.RptLogDto;
import com.example.web_monitor.model.entities.RptlogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RptLogMapper {

    // MapStruct tự động implement logic gán từng field
    RptLogDto toDto(RptlogEntity entity);

    // MapStruct tự động hiểu cần loop qua List và gọi method toDto ở trên
    List<RptLogDto> toDtoList(List<RptlogEntity> entities);

    // (DTO -> Entity)
    RptlogEntity toEntity(RptLogDto dto);

}