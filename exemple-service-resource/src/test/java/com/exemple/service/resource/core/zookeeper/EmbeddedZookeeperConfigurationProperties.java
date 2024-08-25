package com.exemple.service.resource.core.zookeeper;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zookeeper")
public record EmbeddedZookeeperConfigurationProperties(String version) {
}
