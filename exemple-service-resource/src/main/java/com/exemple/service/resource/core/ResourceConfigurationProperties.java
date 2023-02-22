package com.exemple.service.resource.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "resource")
@RequiredArgsConstructor
@Getter
public class ResourceConfigurationProperties {

    private final Cassandra cassandra;

    private final Zookeeper zookeeper;

    @RequiredArgsConstructor
    @Getter
    public static class Cassandra {

        private final String resourceConfiguration;

    }

    @Getter
    public static class Zookeeper {

        private final String host;

        private final int sessionTimeout;

        private final int connectionTimeout;

        private final int retry;

        private final int sleepMsBetweenRetries;

        public Zookeeper(String host,
                @DefaultValue("30000") int sessionTimeout,
                @DefaultValue("10000") int connectionTimeout,
                @DefaultValue("3") int retry,
                @DefaultValue("1000") int sleepMsBetweenRetries) {
            this.host = host;
            this.sessionTimeout = sessionTimeout;
            this.connectionTimeout = connectionTimeout;
            this.sleepMsBetweenRetries = sleepMsBetweenRetries;
            this.retry = retry;
        }

    }

}
