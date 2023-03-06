package com.exemple.service.event.core;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "event")
@Getter
public class EventConfigurationProperties {

    private final Map<String, String> topics;

    private final Long timeout;

    private final Kafka kafka;

    public EventConfigurationProperties(Map<String, String> topics, @DefaultValue("3000") Long timeout, Kafka kafka) {
        this.topics = topics;
        this.timeout = timeout;
        this.kafka = kafka;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Kafka {

        private final String bootstrapServers;

    }

}
