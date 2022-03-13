package com.exemple.service.customer.subscription;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionService {

    boolean save(@NotNull JsonNode source);

    Optional<JsonNode> get(@NotBlank String email);
}
