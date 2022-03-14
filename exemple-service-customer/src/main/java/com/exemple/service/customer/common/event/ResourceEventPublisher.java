package com.exemple.service.customer.common.event;

import com.fasterxml.jackson.databind.JsonNode;

public interface ResourceEventPublisher {

    void publish(JsonNode data, String resource, EventType type);

}
