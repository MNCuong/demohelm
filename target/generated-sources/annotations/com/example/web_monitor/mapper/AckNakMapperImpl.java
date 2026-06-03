package com.example.web_monitor.mapper;

import com.example.web_monitor.dto.AckNakDto;
import com.example.web_monitor.model.entities.AckNakEntity;
import java.time.LocalDateTime;
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
public class AckNakMapperImpl implements AckNakMapper {

    @Override
    public AckNakDto toDto(AckNakEntity entity) {
        if ( entity == null ) {
            return null;
        }

        LocalDateTime actiontime = null;
        Long autoid = null;
        String biccode = null;
        String corerefid = null;
        String msg = null;
        String parentmsg = null;
        String seqid = null;
        String status = null;
        LocalDateTime updatetime = null;

        actiontime = entity.getActiontime();
        autoid = entity.getAutoid();
        biccode = entity.getBiccode();
        corerefid = entity.getCorerefid();
        msg = entity.getMsg();
        parentmsg = entity.getParentmsg();
        seqid = entity.getSeqid();
        status = entity.getStatus();
        updatetime = entity.getUpdatetime();

        String fullnameen = null;
        String shortname = null;

        AckNakDto ackNakDto = new AckNakDto( autoid, seqid, fullnameen, shortname, biccode, status, msg, parentmsg, actiontime, updatetime, corerefid );

        return ackNakDto;
    }

    @Override
    public List<AckNakDto> toDtoList(List<AckNakEntity> entities) {
        if ( entities == null ) {
            return null;
        }

        List<AckNakDto> list = new ArrayList<AckNakDto>( entities.size() );
        for ( AckNakEntity ackNakEntity : entities ) {
            list.add( toDto( ackNakEntity ) );
        }

        return list;
    }

    @Override
    public AckNakEntity toEntity(AckNakDto dto) {
        if ( dto == null ) {
            return null;
        }

        AckNakEntity ackNakEntity = new AckNakEntity();

        ackNakEntity.setActiontime( dto.getActiontime() );
        ackNakEntity.setAutoid( dto.getAutoid() );
        ackNakEntity.setBiccode( dto.getBiccode() );
        ackNakEntity.setCorerefid( dto.getCorerefid() );
        ackNakEntity.setMsg( dto.getMsg() );
        ackNakEntity.setParentmsg( dto.getParentmsg() );
        ackNakEntity.setSeqid( dto.getSeqid() );
        ackNakEntity.setStatus( dto.getStatus() );
        ackNakEntity.setUpdatetime( dto.getUpdatetime() );

        return ackNakEntity;
    }
}
