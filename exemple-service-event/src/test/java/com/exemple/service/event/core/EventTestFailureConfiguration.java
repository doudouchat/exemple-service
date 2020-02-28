package com.exemple.service.event.core;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.mock.env.MockEnvironment;

import kafka.server.KafkaConfig;

@Configuration
@Import(EventConfiguration.class)
public class EventTestFailureConfiguration {

    @Value("${event.kafka.embedded.port}")
    private int kafkaPort;

    @Value("${event.kafka.embedded.dir}")
    private String logDir;

    @Value("${event.topic}")
    private String defaultTopic;

    @Bean
    public EmbeddedKafkaBroker embeddedKafka() {

        EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, true, defaultTopic).brokerProperty(KafkaConfig.LogDirsProp(),
                logDir + "/" + UUID.randomUUID());
        embeddedKafka.kafkaPorts(kafkaPort);

        return embeddedKafka;
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-service-event-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        propertySourcesPlaceholderConfigurer.setEnvironment(environment());
        propertySourcesPlaceholderConfigurer.postProcessBeanFactory(new DefaultListableBeanFactory());
        return propertySourcesPlaceholderConfigurer;
    }

    @Bean
    public static Environment environment() {

        return new MockEnvironment().withProperty("event.timeout", "1");
    }

}
