package com.exemple.service.event.listener;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
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
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import com.fasterxml.jackson.databind.JsonNode;

public class KafkaTestEvent extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(DataEventListener.class);

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Value("${event.topic}")
    private String defaultTopic;

    private KafkaMessageListenerContainer<String, JsonNode> container;

    protected BlockingQueue<ConsumerRecord<String, JsonNode>> records;

    @BeforeClass
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

    @AfterClass
    public void stop() {

        container.stop();

    }

}
