package com.exemple.service.event.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.event.core.EventTestFailureConfiguration;
import com.exemple.service.event.publisher.DataEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { EventTestFailureConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataEventListenerFailureTest extends KafkaTestEvent {

    @Autowired
    private DataEventPublisher dataEventPublisher;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test(expectedExceptions = InterruptedException.class)
    public void publishEventFailure() throws InterruptedException {

        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        JsonNode resource = MAPPER.convertValue(data, JsonNode.class);

        // when publish event
        dataEventPublisher.publish(resource, "account", EventType.CREATE);

        records.poll(10, TimeUnit.SECONDS);

    }

}
