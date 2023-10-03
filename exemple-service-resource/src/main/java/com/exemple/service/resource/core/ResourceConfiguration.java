package com.exemple.service.resource.core;

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
@EnableConfigurationProperties(ResourceConfigurationProperties.class)
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.exemple.service.resource")
@RequiredArgsConstructor
@Slf4j
public class ResourceConfiguration {

    private final ResourceConfigurationProperties resourceProperties;

    @Bean(initMethod = "start", destroyMethod = "close")
    public CuratorFramework resourceCuratorFramework() {

        var client = CuratorFrameworkFactory.newClient(
                resourceProperties.zookeeper().host(),
                resourceProperties.zookeeper().sessionTimeout(),
                resourceProperties.zookeeper().connectionTimeout(),
                new RetryNTimes(resourceProperties.zookeeper().retry(), resourceProperties.zookeeper().sleepMsBetweenRetries()));

        client.getConnectionStateListenable().addListener((c, state) -> LOG.debug("State changed to: {}", state));

        return client;

    }

    @Bean(destroyMethod = "close")
    public CuratorFramework accountCuratorFramework() {

        return resourceCuratorFramework().usingNamespace("account");

    }

}
