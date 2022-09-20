package com.exemple.service.api.launcher.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.exemple.service.application.core.ApplicationConfiguration;
import com.exemple.service.integration.authorization.server.TestAlgorithmConfiguration;
import com.exemple.service.resource.core.ResourceConfiguration;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@Import({ ResourceConfiguration.class, ApplicationConfiguration.class, TestAlgorithmConfiguration.class })
@ComponentScan(basePackages = "com.exemple.service.api.launcher", excludeFilters = @ComponentScan.Filter(SpringBootApplication.class))
public class IntegrationTestConfiguration {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-service-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(@Value("${hazelcast.port:5701}") int port) throws UnknownHostException {

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress(InetAddress.getLocalHost().getHostAddress() + ":" + port);

        return HazelcastClient.newHazelcastClient(config);
    }

}
