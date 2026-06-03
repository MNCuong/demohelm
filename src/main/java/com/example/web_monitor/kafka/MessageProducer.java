package com.example.web_monitor.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
@Slf4j
@Component
public class MessageProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Gửi message chung cho bất kỳ topic nào
     *
     * @param topic   tên topic
     * @param key     key (có thể null)
     * @param message nội dung message (JSON/String)
     */
    public void sendMessage(String topic, String key, String message) {
        log.info("Kafka SEND | topic={} | key={}", topic, key);
        if (key != null) {
            kafkaTemplate.send(topic, key, message);
        } else {
            kafkaTemplate.send(topic, message);
        }
        log.info("Kafka SEND DONE | topic={} | key={}", topic, key);
    }

    public void send(ProducerRecord<String, String> record) {
        kafkaTemplate.send(record);
    }
}
