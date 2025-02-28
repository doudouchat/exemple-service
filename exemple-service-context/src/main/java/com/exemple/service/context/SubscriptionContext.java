package com.exemple.service.context;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Builder;
import lombok.Getter;

@Builder(toBuilder = true)
@Getter
public class SubscriptionContext {

    private final Optional<JsonNode> previousSubscription;

}
