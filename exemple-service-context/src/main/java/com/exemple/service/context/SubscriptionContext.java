package com.exemple.service.context;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public record SubscriptionContext(JsonNode previousSubscription) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final ScopedValue<SubscriptionContext> SUBSCRIPTION_CONTEXT = ScopedValue.newInstance();

    public SubscriptionContext() {
        this(MAPPER.createObjectNode());
    }

}
