package com.exemple.service.event.publisher;

import com.exemple.service.customer.common.event.EventType;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class EventData {

    private final JsonNode data;
    private final String resource;
    private final EventType eventType;
    private final String origin;
    private final String originVersion;
    private final String date;

}
