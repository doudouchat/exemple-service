package com.exemple.service.event.core;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.fasterxml.jackson.databind.JsonNode;

@Configuration
@Profile("!noEvent")
@ComponentScan(basePackages = "com.exemple.service.event")
public class EventConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EventConfiguration.class);

    private final String bootstrapServers;

    private final String defaultTopic;

    public EventConfiguration(@Value("${event.kafka.bootstrap-servers}") String bootstrapServers, @Value("${event.topic}") String defaultTopic) {

        this.bootstrapServers = bootstrapServers;
        this.defaultTopic = defaultTopic;
    }

    @Bean(destroyMethod = "reset")
    public DefaultKafkaProducerFactory<String, JsonNode> producerFactory() {

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

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
