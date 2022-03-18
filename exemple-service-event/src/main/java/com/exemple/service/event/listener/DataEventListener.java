package com.exemple.service.event.listener;

import java.time.OffsetDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.exemple.service.event.publisher.EventData;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataEventListener {

    public static final String X_ORIGIN = "X_Origin";

    public static final String X_ORIGIN_VERSION = "X_Origin_Version";

    public static final String X_RESOURCE = "X_Resource";

    public static final String X_EVENT_TYPE = "X_Event_Type";

    private static final Logger LOG = LoggerFactory.getLogger(DataEventListener.class);

    @Value("${event.timeout:3000}")
    private final Long timeout;

    private final KafkaTemplate<String, JsonNode> template;

    @EventListener
    public void eventConsumer(EventData event) {

        JsonNode data = (JsonNode) event.getSource();
        String resource = event.getResource();

        LOG.debug("send event {} {}", resource, data);

        Message<JsonNode> message = MessageBuilder.withPayload(data)
                .setHeader(KafkaHeaders.TIMESTAMP, OffsetDateTime.parse(event.getDate()).toInstant().toEpochMilli()).setHeader(X_RESOURCE, resource)
                .setHeader(X_EVENT_TYPE, event.getEventType().toString()).setHeader(X_ORIGIN, event.getOrigin())
                .setHeader(X_ORIGIN_VERSION, event.getOriginVersion()).build();

        try {
            template.send(message).get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Kafka Message in topic " + this.template.getDefaultTopic() + " fails in " + timeout + "ms", e);
        }

    }

}
