package com.exemple.service.customer.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class CustomerEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public CustomerEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publish(JsonNode data, String resource, EventType type) {

        ServiceContext context = ServiceContextExecution.context();

        EventData eventData = new EventData(data, resource, type, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);
    }

}
