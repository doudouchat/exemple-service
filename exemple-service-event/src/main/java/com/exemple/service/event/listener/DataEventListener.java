package com.exemple.service.event.listener;

import java.time.OffsetDateTime;
import java.util.concurrent.TimeUnit;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.exemple.service.event.core.EventConfigurationProperties;
import com.exemple.service.event.publisher.EventData;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataEventListener {

    public static final String X_ORIGIN = "X_Origin";

    public static final String X_ORIGIN_VERSION = "X_Origin_Version";

    public static final String X_RESOURCE = "X_Resource";

    public static final String X_EVENT_TYPE = "X_Event_Type";

    private final EventConfigurationProperties eventProperties;

    private final KafkaTemplate<String, JsonNode> template;

    @EventListener
    @SneakyThrows
    public void eventConsumer(EventData event) {

        JsonNode data = (JsonNode) event.getSource();
        String resource = event.getResource();

        LOG.debug("send event {} {}", resource, data);

        Message<JsonNode> message = MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.TIMESTAMP, OffsetDateTime.parse(event.getDate()).toInstant().toEpochMilli()).setHeader(X_RESOURCE, resource)
                .setHeader(X_EVENT_TYPE, event.getEventType().toString()).setHeader(X_ORIGIN, event.getOrigin())
                .setHeader(X_ORIGIN_VERSION, event.getOriginVersion()).build();

        template.send(message).get(eventProperties.getTimeout(), TimeUnit.MILLISECONDS);
    }

}
