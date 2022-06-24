package com.exemple.service.api.launcher.embedded;

import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(value = "port", prefix = "zookeeper.embedded")
@ConditionalOnClass(TestingServer.class)
@RequiredArgsConstructor
@Slf4j
public class EmbeddedZookeeperAutoConfiguration {

    @Value("${zookeeper.embedded.port:-1}")
    private final int port;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TestingServer embeddedZookeeper() throws Exception {

        LOG.info("STARTING EMBEDDED ZOOKEEPER");

        return new TestingServer(port, false);
    }

}
