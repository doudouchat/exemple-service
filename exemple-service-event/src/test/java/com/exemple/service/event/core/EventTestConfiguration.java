package com.exemple.service.event.core;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import kafka.server.KafkaConfig;

@Configuration
@Import({ EventPublisherConfiguration.class, EventKafkaConfiguration.class })
public class EventTestConfiguration {

    @Autowired
    private EventConfigurationProperties eventProperties;

    @Bean
    public EmbeddedKafkaBroker embeddedKafka(@Value("${event.kafka.embedded.port}") int port, @Value("${event.kafka.embedded.dir}") String dir) {

        var topics = eventProperties.topics().values().toArray(new String[eventProperties.topics().size()]);
        var embeddedKafka = new EmbeddedKafkaZKBroker(1, true, topics)
                .brokerProperty(KafkaConfig.LogDirsProp(), dir + "/" + UUID.randomUUID());
        embeddedKafka.kafkaPorts(port);

        return embeddedKafka;
    }

}
