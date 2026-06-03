package com.example.web_monitor.service;


import com.example.web_monitor.dto.KafkaMessageDto;
import com.example.web_monitor.exception.BusinessException;
import com.example.web_monitor.exception.ErrorCode;
import com.example.web_monitor.kafka.MessageConsumer;

import com.example.web_monitor.kafka.MessageProducer;
import com.example.web_monitor.utils.KafkaUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Slf4j
@Service
public class KafkaService {

    private final MessageConsumer messageConsumer;
    private final MessageProducer messageProducer;
    private final MessageService messageService;

    public KafkaService(MessageConsumer messageConsumer, MessageProducer messageProducer,MessageService messageService) {
        this.messageConsumer = messageConsumer;
        this.messageProducer = messageProducer;
        this.messageService = messageService;
    }

    public KafkaMessageDto getKafkaMessageDetail(String key, int  partition) {
        return messageConsumer.findMessageByKey(key, partition);
    }

    public List<KafkaMessageDto> getMessagesFromOffset(int partition, long offset, int limit) {
        return messageConsumer.getMessagesFromOffset(partition, offset, limit);
    }

    public long[] findOffsetRange(int partition, String key, LocalDateTime dateTime) {
        return messageConsumer.findOffsetRange(partition, key, dateTime);
    }

    public long getLatestOffset(int partition) {
        return messageConsumer.getLatestOffset(partition);

    }

    public long getEarliestOffset(int partition) {
        return messageConsumer.getEarliestOffset(partition);
    }
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @Transactional
    public void resendRawMessage(String message) {
        boolean checkCannotResend= KafkaUtils.cannotResend(message);
        if(checkCannotResend){
            throw new BusinessException(ErrorCode.RESEND_OUT_MESSAGE_FORBIDDEN);
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is empty");
        }
        String usrname = KafkaUtils.extractUsrName(message);
        String orgSeqId=KafkaUtils.extractRef20(message);
        messageService.cleanEntityForResend(orgSeqId);
        if (usrname == null) {
            throw new IllegalArgumentException("Cannot extract username from message");
        }
        String topic = KafkaUtils.buildResendTopic(usrname);
        String key = UUID.randomUUID().toString();
        Map<String, String> headers = Map.of(
                "spring_json_header_types", "{\"QUEUE_DESTINATION\":\"java.lang.String\"}",
                "QUEUE_DESTINATION", "disruptor-vm:tcpserver.request"
        );

        try {
            messageService.ensureTopicExists(topic);
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);
            headers.forEach((k, v) -> record.headers().add(new RecordHeader(k, v.getBytes(StandardCharsets.UTF_8))));
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            messageProducer.send(record);
                            log.info("Resent message to topic {} with key {} and usrName {}", topic, key, usrname);

                        }
                    }
            );

        } catch (Exception e) {
            throw new RuntimeException("Kafka resend failed", e);
        }
    }

}
