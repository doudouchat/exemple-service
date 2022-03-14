package com.exemple.service.event.publisher;

import org.springframework.context.ApplicationEvent;

import com.exemple.service.customer.common.event.EventType;
import com.fasterxml.jackson.databind.JsonNode;

public class EventData extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

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

    public String getResource() {
        return resource;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getOrigin() {
        return origin;
    }

    public String getOriginVersion() {
        return originVersion;
    }

    public String getDate() {
        return date;
    }

}
