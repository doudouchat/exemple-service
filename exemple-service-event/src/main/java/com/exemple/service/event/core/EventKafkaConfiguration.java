package com.exemple.service.event.core;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.event.listener")
@RequiredArgsConstructor
@Slf4j
public class EventKafkaConfiguration {

    @Value("${event.kafka.bootstrap-servers}")
    private final String bootstrapServers;

    @Value("${event.topic}")
    private final String defaultTopic;

    @Bean(destroyMethod = "reset")
    public DefaultKafkaProducerFactory<String, JsonNode> producerFactory() {

        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, JsonNode> kafkaTemplate() {
        KafkaTemplate<String, JsonNode> template = new KafkaTemplate<>(producerFactory());
        template.setDefaultTopic(defaultTopic);
        return template;
    }

    @PostConstruct
    public void post() {

        LOG.info("Event services {} is enabled", defaultTopic);

    }

}
