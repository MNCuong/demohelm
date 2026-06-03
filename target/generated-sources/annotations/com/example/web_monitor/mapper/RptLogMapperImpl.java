package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.RptLogDto;
import com.example.web_monitor.model.entities.RptlogEntity;
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
public class RptLogMapperImpl implements RptLogMapper {

    @Override
    public RptLogDto toDto(RptlogEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RptLogDto rptLogDto = new RptLogDto();

        rptLogDto.setAutoid( entity.getAutoid() );
        rptLogDto.setCorerefid( entity.getCorerefid() );
        rptLogDto.setCreatedate( entity.getCreatedate() );
        rptLogDto.setErrmsg( entity.getErrmsg() );
        rptLogDto.setFilename( entity.getFilename() );
        rptLogDto.setMsgtype( entity.getMsgtype() );
        rptLogDto.setParrentmsg( entity.getParrentmsg() );
        rptLogDto.setResend( entity.getResend() );
        rptLogDto.setRptname( entity.getRptname() );
        rptLogDto.setStatus( entity.getStatus() );
        rptLogDto.setSystype( entity.getSystype() );
        rptLogDto.setVsdcode( entity.getVsdcode() );

        return rptLogDto;
    }

    @Override
    public List<RptLogDto> toDtoList(List<RptlogEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<RptLogDto> list = new ArrayList<RptLogDto>( entities.size() );
        for ( RptlogEntity rptlogEntity : entities ) {
            list.add( toDto( rptlogEntity ) );
        }

        return list;
    }

    @Override
    public RptlogEntity toEntity(RptLogDto dto) {
        if ( dto == null ) {
            return null;
        }

        RptlogEntity rptlogEntity = new RptlogEntity();

        rptlogEntity.setAutoid( dto.getAutoid() );
        rptlogEntity.setCorerefid( dto.getCorerefid() );
        rptlogEntity.setCreatedate( dto.getCreatedate() );
        rptlogEntity.setErrmsg( dto.getErrmsg() );
        rptlogEntity.setFilename( dto.getFilename() );
        rptlogEntity.setMsgtype( dto.getMsgtype() );
        rptlogEntity.setParrentmsg( dto.getParrentmsg() );
        rptlogEntity.setResend( dto.getResend() );
        rptlogEntity.setRptname( dto.getRptname() );
        rptlogEntity.setStatus( dto.getStatus() );
        rptlogEntity.setSystype( dto.getSystype() );
        rptlogEntity.setVsdcode( dto.getVsdcode() );

        return rptlogEntity;
    }
}
