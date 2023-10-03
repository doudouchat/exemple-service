package com.exemple.service.event.core;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "event")
public record EventConfigurationProperties(Map<String, String> topics,
                                           @DefaultValue("3000") Long timeout,
                                           Kafka kafka) {

    public static record Kafka(String bootstrapServers) {

    }

}
