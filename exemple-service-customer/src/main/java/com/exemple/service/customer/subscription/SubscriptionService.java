package com.exemple.service.customer.subscription;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface SubscriptionService {

    void create(@NotBlank String email, @NotNull JsonNode source);

    void update(@NotBlank String email, @NotNull JsonNode source);

    Optional<JsonNode> get(@NotBlank String email);
}
