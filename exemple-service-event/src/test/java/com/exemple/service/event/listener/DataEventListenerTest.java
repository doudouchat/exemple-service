package com.exemple.service.event.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.exemple.service.event.core.EventTestConfiguration;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { EventTestConfiguration.class })
public class DataEventListenerTest extends KafkaTestEvent {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OffsetDateTime date = OffsetDateTime.now();

    @Test
    public void publishEvent() throws InterruptedException {

        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        String origin = "app1";
        String originVersion = "v1";

        JsonNode resource = MAPPER.convertValue(data, JsonNode.class);

        EventData eventData = new EventData(resource, "account", EventType.CREATE, origin, originVersion, date.toString());
        applicationEventPublisher.publishEvent(eventData);

        ConsumerRecord<String, JsonNode> received = records.poll(10, TimeUnit.SECONDS);

        assertThat(received, is(notNullValue()));
        assertThat(received.value(), is(resource));
        try (StringDeserializer deserializer = new StringDeserializer()) {
            assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_ORIGIN_VERSION).value()), is(originVersion));
            assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_ORIGIN).value()), is(origin));
            assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_RESOURCE).value()), is("account"));
            assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_EVENT_TYPE).value()),
                    is(EventType.CREATE.toString()));
            assertThat(received.timestamp(), is(date.toInstant().toEpochMilli()));
        }

    }

}
