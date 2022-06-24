package com.exemple.service.api.launcher.embedded;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import kafka.server.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConditionalOnProperty(value = { "port", "dir", "defaultTopic" }, prefix = "kafka.embedded")
@ConditionalOnClass(EmbeddedKafkaBroker.class)
@RequiredArgsConstructor
@Slf4j
public class EmbeddedKafkaAutoConfiguration {

    @Value("${kafka.embedded.port:0}")
    private final int kafkaPort;

    @Value("${kafka.embedded.dir:#{systemProperties['java.io.tmpdir']}}")
    private final String logDir;

    @Value("${kafka.embedded.defaultTopic:...}")
    private final String[] defaultTopic;

    @Bean(destroyMethod = "destroy")
    public EmbeddedKafkaBroker embeddedKafka() {

        EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaBroker(1, false, defaultTopic).brokerProperty(KafkaConfig.LogDirsProp(),
                logDir + "/" + UUID.randomUUID());
        embeddedKafka.kafkaPorts(kafkaPort);

        LOG.info("STARTING EMBEDDED KAFKA");

        return embeddedKafka;
    }

}
