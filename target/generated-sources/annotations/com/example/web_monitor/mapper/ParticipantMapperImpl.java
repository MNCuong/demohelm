package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.ParticipantDto;
import com.example.web_monitor.model.entities.ParticipantEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T15:36:18+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class ParticipantMapperImpl implements ParticipantMapper {

    @Override
    public ParticipantEntity toEntity(ParticipantDto dto) {
        if ( dto == null ) {
            return null;
        }

        ParticipantEntity.ParticipantEntityBuilder participantEntity = ParticipantEntity.builder();

        participantEntity.biccode( dto.getBiccode() );
        participantEntity.consumers( dto.getConsumers() );
        participantEntity.fullnameen( dto.getFullnameen() );
        participantEntity.fullnamevn( dto.getFullnamevn() );
        if ( dto.getGroupid() != null ) {
            participantEntity.groupid( Long.parseLong( dto.getGroupid() ) );
        }
        participantEntity.groupqueue( dto.getGroupqueue() );
        participantEntity.id( dto.getId() );
        participantEntity.shortname( dto.getShortname() );
        participantEntity.status( dto.getStatus() );
        participantEntity.userid( dto.getUserid() );
        participantEntity.usertype( dto.getUsertype() );

        return participantEntity.build();
    }

    @Override
    public ParticipantDto toDto(ParticipantEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ParticipantDto participantDto = new ParticipantDto();

        participantDto.setBiccode( entity.getBiccode() );
        participantDto.setConsumers( entity.getConsumers() );
        participantDto.setFullnameen( entity.getFullnameen() );
        participantDto.setFullnamevn( entity.getFullnamevn() );
        if ( entity.getGroupid() != null ) {
            participantDto.setGroupid( String.valueOf( entity.getGroupid() ) );
        }
        participantDto.setGroupqueue( entity.getGroupqueue() );
        participantDto.setId( entity.getId() );
        participantDto.setShortname( entity.getShortname() );
        participantDto.setStatus( entity.getStatus() );
        participantDto.setUpdatetime( entity.getUpdatetime() );
        participantDto.setUserid( entity.getUserid() );
        participantDto.setUsertype( entity.getUsertype() );
        participantDto.setVsdcode( entity.getVsdcode() );

        return participantDto;
    }
}
