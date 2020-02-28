package com.exemple.service.event.core;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import kafka.server.KafkaConfig;

@Configuration
@Import(EventConfiguration.class)
public class EventTestConfiguration {

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
        return propertySourcesPlaceholderConfigurer;
    }

}
