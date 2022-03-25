package com.exemple.service.event.listener;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.event.core.EventTestConfiguration;
import com.exemple.service.event.publisher.DataEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(EventTestConfiguration.class)
public class DataEventListenerTest extends KafkaTestEvent {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OffsetDateTime date = OffsetDateTime.now();

    @Autowired
    private DataEventPublisher dataEventPublisher;

    @Test
    public void publishEvent() throws InterruptedException, IOException {

        // Given build message

        JsonNode resource = MAPPER.readTree("{\"key1\": \"value1\", \"key2\": \"value2\"}");

        // and build context
        String origin = "app1";
        String originVersion = "v1";
        ServiceContextExecution.context().setDate(date);
        ServiceContextExecution.context().setApp(origin);
        ServiceContextExecution.context().setVersion(originVersion);

        // when publish event
        dataEventPublisher.publish(resource, "account", EventType.CREATE);

        // Then check message
        ConsumerRecord<String, JsonNode> received = records.poll(10, TimeUnit.SECONDS);

        assertAll(
                () -> assertThat(received, is(notNullValue())),
                () -> assertThat(received.value(), is(resource)),
                () -> assertThat(received.timestamp(), is(date.toInstant().toEpochMilli())));
        try (StringDeserializer deserializer = new StringDeserializer()) {
            assertAll(
                    () -> assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_ORIGIN_VERSION).value()),
                            is(originVersion)),
                    () -> assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_ORIGIN).value()),
                            is(origin)),
                    () -> assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_RESOURCE).value()),
                            is("account")),
                    () -> assertThat(deserializer.deserialize(null, received.headers().lastHeader(DataEventListener.X_EVENT_TYPE).value()),
                            is(EventType.CREATE.toString())));
        }

    }

}
