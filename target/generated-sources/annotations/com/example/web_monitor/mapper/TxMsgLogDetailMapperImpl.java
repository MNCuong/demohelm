package com.example.web_monitor.mapper;

import com.example.web_monitor.model.entities.TxMsgLogDetail;
import com.example.web_monitor.model.entities.TxMsgLogDetailHistEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-03T15:36:19+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class TxMsgLogDetailMapperImpl implements TxMsgLogDetailMapper {

    @Override
    public TxMsgLogDetailHistEntity toHistEntity(TxMsgLogDetail entity) {
        if ( entity == null ) {
            return null;
        }

        TxMsgLogDetailHistEntity txMsgLogDetailHistEntity = new TxMsgLogDetailHistEntity();

        txMsgLogDetailHistEntity.setAutoId( entity.getAutoId() );
        txMsgLogDetailHistEntity.setOrgSeqId( entity.getOrgSeqId() );
        txMsgLogDetailHistEntity.setStatus( entity.getStatus() );
        txMsgLogDetailHistEntity.setTxDate( entity.getTxDate() );

        return txMsgLogDetailHistEntity;
    }
}
