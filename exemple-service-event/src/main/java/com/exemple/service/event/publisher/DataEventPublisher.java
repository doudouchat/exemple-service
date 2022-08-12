package com.exemple.service.event.publisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component("resourceEventPublisher")
@RequiredArgsConstructor
public class DataEventPublisher implements ResourceEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(JsonNode data, String resource, EventType type) {

        var context = ServiceContextExecution.context();

        var eventData = new EventData(data, resource, type, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);
    }

}
