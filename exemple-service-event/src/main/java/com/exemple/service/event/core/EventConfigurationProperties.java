package com.exemple.service.event.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "event")
@Getter
public class EventConfigurationProperties {

    private final String topic;

    private final Long timeout;

    private final Kafka kafka;

    public EventConfigurationProperties(String topic, @DefaultValue("3000") Long timeout, Kafka kafka) {
        this.topic = topic;
        this.timeout = timeout;
        this.kafka = kafka;
    }

    @RequiredArgsConstructor
    @Getter
    public static class Kafka {

        private final String bootstrapServers;

    }

}
