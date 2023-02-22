package com.exemple.service.store.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties(StoreConfigurationProperties.class)
@ComponentScan(basePackages = "com.exemple.service.store")
@RequiredArgsConstructor
@Slf4j
public class StoreConfiguration {

    private final StoreConfigurationProperties storeProperties;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework storeCuratorFramework() {

        var client = CuratorFrameworkFactory.newClient(
                storeProperties.getZookeeper().getHost(),
                storeProperties.getZookeeper().getSessionTimeout(),
                storeProperties.getZookeeper().getConnectionTimeout(),
                new RetryNTimes(storeProperties.getZookeeper().getRetry(), storeProperties.getZookeeper().getSleepMsBetweenRetries()));

        client.getConnectionStateListenable().addListener((c, state) -> LOG.debug("State changed to: {}", state));

        return client;

    }

    @Bean(destroyMethod = "close")
    public CuratorFramework stockCuratorFramework() {

        return storeCuratorFramework().usingNamespace("stock");

    }

}
