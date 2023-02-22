package com.exemple.service.resource.core.cassandra;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "resource.cassandra")
@RequiredArgsConstructor
@Getter
public class EmbeddedCassandraConfigurationProperties {

    private final String version;
}
