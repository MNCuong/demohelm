package com.example.web_monitor.mapper;

import com.example.web_monitor.model.entities.MessageEntity;
import com.example.web_monitor.model.entities.TxMsgLogDetail;
import com.example.web_monitor.model.entities.TxMsgLogDetailHistEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TxMsgLogDetailMapper {

    @Mapping(target = "id", ignore = true)
    TxMsgLogDetailHistEntity toHistEntity(TxMsgLogDetail entity);
}
