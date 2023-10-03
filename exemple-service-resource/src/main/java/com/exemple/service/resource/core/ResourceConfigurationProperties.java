package com.exemple.service.resource.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "resource")
public record ResourceConfigurationProperties(Cassandra cassandra,
                                              Zookeeper zookeeper) {

    public static record Cassandra(String resourceConfiguration) {

    }

    public record Zookeeper(String host,
                            @DefaultValue("30000") int sessionTimeout,
                            @DefaultValue("10000") int connectionTimeout,
                            @DefaultValue("3") int retry,
                            @DefaultValue("1000") int sleepMsBetweenRetries) {

    }

}
