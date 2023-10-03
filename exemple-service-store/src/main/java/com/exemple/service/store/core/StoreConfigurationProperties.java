package com.exemple.service.store.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "store")
public record StoreConfigurationProperties(Zookeeper zookeeper) {

    public record Zookeeper(String host,
                            @DefaultValue("30000") int sessionTimeout,
                            @DefaultValue("10000") int connectionTimeout,
                            @DefaultValue("3") int retry,
                            @DefaultValue("1000") int sleepMsBetweenRetries) {
    }

}
