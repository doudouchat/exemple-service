package com.exemple.service.api.core.embedded;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import kafka.server.KafkaConfig;

@Configuration
@ConditionalOnProperty(value = { "port", "dir", "defaultTopic" }, prefix = "kafka.embedded")
public class KafkaConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfiguration.class);

    private final int kafkaPort;

    private final String logDir;

    private final String defaultTopic;

    public KafkaConfiguration(@Value("${kafka.embedded.port}") int kafkaPort, @Value("${kafka.embedded.dir}") String logDir,
            @Value("${kafka.embedded.defaultTopic}") String defaultTopic) {
        this.kafkaPort = kafkaPort;
        this.logDir = logDir;
        this.defaultTopic = defaultTopic;

    }

    @Bean(destroyMethod = "destroy")
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

}
