package com.exemple.service.api.core.embedded;

import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;

@Configuration
@ConditionalOnProperty(value = "port", prefix = "zookeeper.embedded")
@ConditionalOnClass(TestingServer.class)
@RequiredArgsConstructor
public class EmbeddedZookeeperAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedZookeeperAutoConfiguration.class);

    @Value("${zookeeper.embedded.port:-1}")
    private final int port;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TestingServer embeddedZookeeper() throws Exception {

        LOG.info("STARTING EMBEDDED ZOOKEEPER");

        return new TestingServer(port, false);
    }

}
