package com.exemple.service.api.core.embedded;

import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(TestingServer.class)
public class ZookeeperConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ZookeeperConfiguration.class);

    private final int port;

    public ZookeeperConfiguration(@Value("${zookeeper.embedded.port:-1}") int port) {
        this.port = port;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public TestingServer embeddedZookeeper() throws Exception {

        LOG.info("STARTING EMBEDDED ZOOKEEPER");

        return new TestingServer(port, false);
    }

}
