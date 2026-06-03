package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.MessageDto;
import com.example.web_monitor.model.entities.MessageEntity;
import com.example.web_monitor.model.entities.MessageHistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

// componentModel = "spring" giúp Inject Mapper này như một @Component/@Service
// unmappedTargetPolicy = ReportingPolicy.IGNORE giúp bỏ qua cảnh báo nếu có field không được map
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MessageMapper {

    // MapStruct tự động implement logic gán từng field
    MessageDto toDto(MessageEntity entity);

    // MapStruct tự động hiểu cần loop qua List và gọi method toDto ở trên
    List<MessageDto> toDtoList(List<MessageEntity> entities);

    // (DTO -> Entity)
    MessageEntity toEntity(MessageDto dto);

    MessageHistEntity toHistEntity(MessageEntity entity);
}