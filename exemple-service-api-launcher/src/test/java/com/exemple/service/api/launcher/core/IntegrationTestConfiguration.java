package com.exemple.service.api.launcher.core;

import java.util.Collection;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.exemple.service.application.core.ApplicationConfiguration;
import com.exemple.service.integration.authorization.server.TestAlgorithmConfiguration;
import com.exemple.service.resource.core.ResourceConfiguration;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;

import jakarta.annotation.PostConstruct;

@Configuration
@Import({ ResourceConfiguration.class, ApplicationConfiguration.class, TestAlgorithmConfiguration.class })
@ComponentScan(basePackages = "com.exemple.service.api.launcher", excludeFilters = @ComponentScan.Filter(SpringBootApplication.class))
public class IntegrationTestConfiguration {

    @Value("${event.topics.account}")
    private String accountTopic;

    @Value("${event.topics.subscription}")
    private String subscriptionTopic;

    @Autowired
    private KafkaConsumer<?, ?> consumerEvent;

    @Bean
    public HazelcastInstance hazelcastInstance(@Value("${hazelcast.port:5701}") int port) {

        ClientConfig config = new ClientConfig();
        config.getNetworkConfig().addAddress("127.0.0.1:" + port);

        return HazelcastClient.newHazelcastClient(config);
    }

    @PostConstruct
    public void suscribeConsumerEvent() {

        consumerEvent.subscribe(List.of(accountTopic, subscriptionTopic).stream().distinct().toList(), new ConsumerRebalanceListener() {

            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // NOP
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                consumerEvent.seekToBeginning(partitions);
            }
        });
    }

}
