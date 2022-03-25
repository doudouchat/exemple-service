package com.exemple.service.event.listener;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.fasterxml.jackson.databind.JsonNode;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class KafkaTestEvent {

    private static final Logger LOG = LoggerFactory.getLogger(DataEventListener.class);

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Value("${event.topic}")
    private String defaultTopic;

    private KafkaMessageListenerContainer<String, JsonNode> container;

    protected BlockingQueue<ConsumerRecord<String, JsonNode>> records;

    @BeforeAll
    public void createConsumer() throws Exception {

        records = new LinkedBlockingQueue<>();

        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("sender", "false", embeddedKafka);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, "foo");
        DefaultKafkaConsumerFactory<String, JsonNode> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProperties,
                new StringDeserializer(), new JsonDeserializer<>(JsonNode.class, false));
        ContainerProperties containerProperties = new ContainerProperties(defaultTopic);
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

        MessageListener<String, JsonNode> listener = data -> {

            LOG.debug("data: {}, header: {}", data.value(), StreamSupport.stream(data.headers().spliterator(), false)
                    .map(h -> h.key() + ":" + new String(h.value())).collect(Collectors.toList()));
            records.add(data);
        };

        container.setupMessageListener(listener);

        container.start();

        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    @AfterAll
    public void stop() {

        container.stop();

    }

}
