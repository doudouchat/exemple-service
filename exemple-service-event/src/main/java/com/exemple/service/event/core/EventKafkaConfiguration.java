package com.exemple.service.event.core;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableConfigurationProperties(EventConfigurationProperties.class)
@ComponentScan(basePackages = "com.exemple.service.event.listener")
@RequiredArgsConstructor
@Slf4j
public class EventKafkaConfiguration {

    private final EventConfigurationProperties eventProperties;

    @Bean(destroyMethod = "reset")
    public DefaultKafkaProducerFactory<String, JsonNode> producerFactory() {

        Map<String, Object> props = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, eventProperties.kafka().bootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, JsonNode> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @PostConstruct
    public void post() {
        eventProperties.topics().entrySet()
                .forEach((Entry<String, String> topic) -> LOG.info("Event services {}:{} is enabled", topic.getKey(), topic.getValue()));

    }

}
