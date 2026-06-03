package com.example.web_monitor.configuration;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaBeanConfig {

    private ConsumerFactory<String, String> consumerFactory;
    public KafkaBeanConfig(ConsumerFactory<String, String> comConsumerFactory){
        this.consumerFactory=comConsumerFactory;
    }
    @Bean
    @Scope("prototype")
    public KafkaConsumer<String, String> kafkaConsumer() {
        return (KafkaConsumer<String, String>) consumerFactory.createConsumer();
    }
}