package com.exemple.service.context;

import java.util.Optional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

public final class SubscriptionContextExecution {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ThreadLocal<SubscriptionContextExecution> executionContext = ThreadLocal.withInitial(SubscriptionContextExecution::new);

    private SubscriptionContext model;

    private SubscriptionContextExecution() {

        this.model = SubscriptionContext.builder()
                .previousSubscription(Optional.empty())
                .build();
    }

    private void reset(SubscriptionContext model) {
        this.model = model;
    }

    public static void setPreviousSubscription(JsonNode previousSubscription) {

        var model = executionContext.get().model.toBuilder().previousSubscription(Optional.of(previousSubscription)).build();
        executionContext.get().reset(model);
    }

    public static JsonNode getPreviousSubscription() {

        return executionContext.get().model.getPreviousSubscription().orElseGet(MAPPER::createObjectNode);
    }

    public static void destroy() {

        executionContext.remove();
    }

}
