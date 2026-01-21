package com.exemple.service.customer.subscription;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public interface SubscriptionService {

    void create(@NotBlank String email, @NotNull JsonNode source);

    void update(@NotBlank String email, @NotNull JsonNode source);

    Optional<JsonNode> get(@NotBlank String email);
}
