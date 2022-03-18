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
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.exemple.service.store")
@RequiredArgsConstructor
public class StoreConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(StoreConfiguration.class);

    @Value("${store.zookeeper.host}")
    private final String address;

    @Value("${store.zookeeper.sessionTimeout:30000}")
    private final int sessionTimeout;

    @Value("${store.zookeeper.connectionTimeout:10000}")
    private final int connectionTimeout;

    @Value("${store.zookeeper.retry:3}")
    private final int retry;

    @Value("${store.zookeeper.sleepMsBetweenRetries:1000}")
    private final int sleepMsBetweenRetries;

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
