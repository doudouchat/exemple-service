package com.exemple.service.resource.core.cassandra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "resource.cassandra")
public record EmbeddedCassandraConfigurationProperties(String version) {
}
