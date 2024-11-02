package com.exemple.service.application.core.zookeeper;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Testcontainers
@EnableConfigurationProperties(EmbeddedZookeeperConfigurationProperties.class)
@RequiredArgsConstructor
@Slf4j
public class EmbeddedZookeeperConfiguration {

    private final EmbeddedZookeeperConfigurationProperties properties;

    @Bean
    @ServiceConnection
    public GenericContainer embeddedZookeeper() {

        return new GenericContainer<>("zookeeper:" + properties.version())
                .withExposedPorts(2181)
                .withLogConsumer(new Slf4jLogConsumer(LOG));
    }

}
