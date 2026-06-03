package com.example.web_monitor.service;

import com.example.web_monitor.model.entities.SentMsgEntity;
import com.example.web_monitor.repository.SentMsgRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SentMsgService {
    private static final Logger log = LoggerFactory.getLogger(SentMsgService.class);
    private final SentMsgRepository  sentMsgRepository;
    public SentMsgService(SentMsgRepository sentMsgRepository) {
        this.sentMsgRepository = sentMsgRepository;
    }
    public Optional<SentMsgEntity> findByCoreRefId(String seqId){
        return sentMsgRepository.findByCoreRefIdOrderByAutoIdDesc(seqId);
    }
    public Optional<SentMsgEntity> findByAutoId(
            String autoid, String refSender
    ) {
        log.info("AUTOID_{} - Sender_{}", autoid,refSender);
        return sentMsgRepository
                .findByAutoId(autoid,refSender);
    }

}
