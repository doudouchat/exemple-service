package com.exemple.service.resource.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.exemple.service.resource")
@RequiredArgsConstructor
@Slf4j
public class ResourceConfiguration {

    @Value("${resource.zookeeper.host}")
    private final String address;

    @Value("${resource.zookeeper.sessionTimeout:30000}")
    private final int sessionTimeout;

    @Value("${resource.zookeeper.connectionTimeout:10000}")
    private final int connectionTimeout;

    @Value("${resource.zookeeper.retry:3}")
    private final int retry;

    @Value("${resource.zookeeper.sleepMsBetweenRetries:1000}")
    private final int sleepMsBetweenRetries;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework resourceCuratorFramework() {

        var client = CuratorFrameworkFactory.newClient(address, sessionTimeout, connectionTimeout, new RetryNTimes(retry, sleepMsBetweenRetries));

        client.getConnectionStateListenable().addListener((c, state) -> LOG.debug("State changed to: {}", state));

        return client;

    }

    @Bean(destroyMethod = "close")
    public CuratorFramework accountCuratorFramework() {

        return resourceCuratorFramework().usingNamespace("account");

    }

}
