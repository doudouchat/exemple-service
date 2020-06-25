package com.exemple.service.api.core.embedded;

import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import kafka.server.KafkaConfig;

@Configuration
@ConditionalOnProperty(value = { "port", "dir", "defaultTopic" }, prefix = "api.embedded.kafka")
public class KafkaConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);

    private final int kafkaPort;

    private final String logDir;

    private final String defaultTopic;

    public KafkaConfiguration(@Value("${api.embedded.kafka.port}") int kafkaPort, @Value("${api.embedded.kafka.dir}") String logDir,
            @Value("${api.embedded.kafka.defaultTopic}") String defaultTopic) {
        this.kafkaPort = kafkaPort;
        this.logDir = logDir;
        this.defaultTopic = defaultTopic;

    }

    @Bean
    public EmbeddedKafkaBroker embeddedKafka() {

        EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, false, defaultTopic).brokerProperty(KafkaConfig.LogDirsProp(),
                logDir + "/" + UUID.randomUUID());
        embeddedKafka.kafkaPorts(kafkaPort);

        return embeddedKafka;
    }

    @PostConstruct
    public void startKafka() {

        LOG.info("STARTING EMBEDDED KAFKA");

        embeddedKafka().afterPropertiesSet();
    }

    @PreDestroy
    public void closeKafka() {

        embeddedKafka().destroy();
    }

}
