package br.com.tareffa.worker.config.kafka;

import org.springframework.context.annotation.Configuration;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {
    
    @Autowired
    KafkaProperties properties;

    @Bean
    public Map<String, Object> producerConfigs() {
        System.out.println("");
        System.out.println(properties.getUrl());
        Map<String, Object> config = (Map) properties.buildDefaults();
        return config;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}