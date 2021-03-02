package com.exemple.service.api.core.embedded;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import kafka.server.KafkaConfig;

@Configuration
@ConditionalOnProperty(value = { "port", "dir", "defaultTopic" }, prefix = "kafka.embedded")
@ConditionalOnClass(EmbeddedKafkaBroker.class)
public class EmbeddedKafkaAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedKafkaAutoConfiguration.class);

    private final int kafkaPort;

    private final String logDir;

    private final String[] defaultTopic;

    public EmbeddedKafkaAutoConfiguration(@Value("${kafka.embedded.port:0}") int kafkaPort,
            @Value("${kafka.embedded.dir:#{systemProperties['java.io.tmpdir']}}") String logDir,
            @Value("${kafka.embedded.defaultTopic:...}") String[] defaultTopic) {
        this.kafkaPort = kafkaPort;
        this.logDir = logDir;
        this.defaultTopic = defaultTopic.clone();

    }

    @Bean(destroyMethod = "destroy")
    public EmbeddedKafkaBroker embeddedKafka() {

        EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, false, defaultTopic).brokerProperty(KafkaConfig.LogDirsProp(),
                logDir + "/" + UUID.randomUUID());
        embeddedKafka.kafkaPorts(kafkaPort);

        LOG.info("STARTING EMBEDDED KAFKA");

        return embeddedKafka;
    }

}
