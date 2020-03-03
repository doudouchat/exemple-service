package com.exemple.service.store.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.store")
public class StoreConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(StoreConfiguration.class);

    private final String address;

    private final int sessionTimeout;

    private final int connectionTimeout;

    private final int retry;

    private final int sleepMsBetweenRetries;

    public StoreConfiguration(@Value("${store.zookeeper.host}") String address, @Value("${store.zookeeper.sessionTimeout:30000}") int sessionTimeout,
            @Value("${store.zookeeper.connectionTimeout:10000}") int connectionTimeout, @Value("${store.zookeeper.retry:3}") int retry,
            @Value("${store.zookeeper.sleepMsBetweenRetries:1000}") int sleepMsBetweenRetries) {

        this.address = address;
        this.sessionTimeout = sessionTimeout;
        this.connectionTimeout = connectionTimeout;
        this.retry = retry;
        this.sleepMsBetweenRetries = sleepMsBetweenRetries;
    }

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework storeCuratorFramework() {

        CuratorFramework client = CuratorFrameworkFactory.newClient(address, sessionTimeout, connectionTimeout,
                new RetryNTimes(retry, sleepMsBetweenRetries));

        client.getConnectionStateListenable().addListener((c, state) -> LOG.debug("State changed to: {}", state));

        return client;

    }

    @Bean(destroyMethod = "close")
    public CuratorFramework stockCuratorFramework() {

        return storeCuratorFramework().usingNamespace("stock");

    }

}
