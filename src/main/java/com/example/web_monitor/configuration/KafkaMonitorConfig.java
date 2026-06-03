package com.example.web_monitor.configuration;

import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaMonitorConfig {

    @Bean(name = "monitorConsumer", destroyMethod = "close")
    public Consumer<String, String> monitorConsumer(ConsumerFactory<String, String> consumerFactory) {
        // Tạo một consumer từ factory có sẵn
        return consumerFactory.createConsumer();
    }
}