package com.exemple.service.context;

import java.util.Optional;

import lombok.Builder;
import lombok.Getter;
import tools.jackson.databind.JsonNode;

@Builder(toBuilder = true)
@Getter
public class SubscriptionContext {

    private final Optional<JsonNode> previousSubscription;

}
