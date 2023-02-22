package com.exemple.service.application.core;

import org.apache.curator.test.TestingServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ApplicationConfiguration.class)
public class ApplicationTestConfiguration {

    @Bean(destroyMethod = "stop")
    public TestingServer embeddedZookeeper(@Value("${application.zookeeper.port}") int port) throws Exception {

        return new TestingServer(port, true);
    }
}
