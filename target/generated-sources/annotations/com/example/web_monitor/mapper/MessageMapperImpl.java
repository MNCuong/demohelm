package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.MessageDto;
import com.example.web_monitor.model.entities.MessageEntity;
import com.example.web_monitor.model.entities.MessageHistEntity;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T15:36:18+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageDto toDto(MessageEntity entity) {
        if ( entity == null ) {
            return null;
        }

        MessageDto.MessageDtoBuilder messageDto = MessageDto.builder();

        messageDto.autoId( entity.getAutoId() );
        messageDto.coreRefId( entity.getCoreRefId() );
        messageDto.createTst( entity.getCreateTst() );
        messageDto.feedback( entity.getFeedback() );
        messageDto.inOutFlag( entity.getInOutFlag() );
        messageDto.msgBody( entity.getMsgBody() );
        messageDto.orgSeqId( entity.getOrgSeqId() );
        messageDto.refCode( entity.getRefCode() );
        messageDto.refReceiver( entity.getRefReceiver() );
        messageDto.refSender( entity.getRefSender() );
        messageDto.refSeqId( entity.getRefSeqId() );
        messageDto.seqId( entity.getSeqId() );
        messageDto.status( entity.getStatus() );
        messageDto.stpRefId( entity.getStpRefId() );
        messageDto.trfCode( entity.getTrfCode() );
        messageDto.txDate( entity.getTxDate() );
        messageDto.usrName( entity.getUsrName() );

        return messageDto.build();
    }

    @Override
    public List<MessageDto> toDtoList(List<MessageEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<MessageDto> list = new ArrayList<MessageDto>( entities.size() );
        for ( MessageEntity messageEntity : entities ) {
            list.add( toDto( messageEntity ) );
        }

        return list;
    }

    @Override
    public MessageEntity toEntity(MessageDto dto) {
        if ( dto == null ) {
            return null;
        }

        MessageEntity messageEntity = new MessageEntity();

        messageEntity.setAutoId( dto.getAutoId() );
        messageEntity.setCoreRefId( dto.getCoreRefId() );
        messageEntity.setCreateTst( dto.getCreateTst() );
        messageEntity.setFeedback( dto.getFeedback() );
        messageEntity.setInOutFlag( dto.getInOutFlag() );
        messageEntity.setMsgBody( dto.getMsgBody() );
        messageEntity.setOrgSeqId( dto.getOrgSeqId() );
        messageEntity.setRefCode( dto.getRefCode() );
        messageEntity.setRefReceiver( dto.getRefReceiver() );
        messageEntity.setRefSender( dto.getRefSender() );
        messageEntity.setRefSeqId( dto.getRefSeqId() );
        messageEntity.setSeqId( dto.getSeqId() );
        messageEntity.setStatus( dto.getStatus() );
        messageEntity.setStpRefId( dto.getStpRefId() );
        messageEntity.setTrfCode( dto.getTrfCode() );
        messageEntity.setTxDate( dto.getTxDate() );
        messageEntity.setUsrName( dto.getUsrName() );

        return messageEntity;
    }

    @Override
    public MessageHistEntity toHistEntity(MessageEntity entity) {
        if ( entity == null ) {
            return null;
        }

        MessageHistEntity messageHistEntity = new MessageHistEntity();

        messageHistEntity.setAutoId( entity.getAutoId() );
        messageHistEntity.setBankRefId( entity.getBankRefId() );
        messageHistEntity.setCoreRefId( entity.getCoreRefId() );
        messageHistEntity.setCreateTst( entity.getCreateTst() );
        messageHistEntity.setErrCode( entity.getErrCode() );
        messageHistEntity.setErrMsg( entity.getErrMsg() );
        messageHistEntity.setFeedback( entity.getFeedback() );
        messageHistEntity.setInOutFlag( entity.getInOutFlag() );
        messageHistEntity.setIpAddress( entity.getIpAddress() );
        messageHistEntity.setMsgBody( entity.getMsgBody() );
        messageHistEntity.setMsgCore( entity.getMsgCore() );
        messageHistEntity.setOrgSeqId( entity.getOrgSeqId() );
        messageHistEntity.setRefCode( entity.getRefCode() );
        messageHistEntity.setRefOrgSeqId21( entity.getRefOrgSeqId21() );
        messageHistEntity.setRefOwner( entity.getRefOwner() );
        messageHistEntity.setRefQtty( entity.getRefQtty() );
        messageHistEntity.setRefReceiver( entity.getRefReceiver() );
        messageHistEntity.setRefSender( entity.getRefSender() );
        messageHistEntity.setRefSeqId( entity.getRefSeqId() );
        messageHistEntity.setRefSymbol( entity.getRefSymbol() );
        messageHistEntity.setSendTst( entity.getSendTst() );
        messageHistEntity.setSeqId( entity.getSeqId() );
        messageHistEntity.setStatus( entity.getStatus() );
        messageHistEntity.setTrfCode( entity.getTrfCode() );
        messageHistEntity.setTxDate( entity.getTxDate() );
        messageHistEntity.setTxNum( entity.getTxNum() );
        messageHistEntity.setUsrName( entity.getUsrName() );

        return messageHistEntity;
    }
}
