package com.exemple.service.event.listener;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.exemple.service.event.core.EventTestFailureConfiguration;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { EventTestFailureConfiguration.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataEventListenerFailureTest extends KafkaTestEvent {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private OffsetDateTime date = OffsetDateTime.now();

    @Test(expectedExceptions = InterruptedException.class)
    public void publishEventFailure() throws InterruptedException {

        Map<String, Object> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");

        String origin = "app1";
        String originVersion = "v1";

        JsonNode resource = MAPPER.convertValue(data, JsonNode.class);

        EventData eventData = new EventData(resource, "account", EventType.CREATE, origin, originVersion, date.toString());
        applicationEventPublisher.publishEvent(eventData);

        records.poll(10, TimeUnit.SECONDS);

    }

}
