package com.exemple.service.event.publisher;

import org.springframework.context.ApplicationEvent;

import com.exemple.service.customer.common.event.EventType;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class EventData extends ApplicationEvent {

    private final String resource;
    private final EventType eventType;
    private final String origin;
    private final String originVersion;
    private final String date;

    public EventData(JsonNode data, String resource, EventType eventType, String origin, String originVersion, String date) {

        super(data);

        this.resource = resource;
        this.eventType = eventType;
        this.origin = origin;
        this.originVersion = originVersion;
        this.date = date;
    }

}
