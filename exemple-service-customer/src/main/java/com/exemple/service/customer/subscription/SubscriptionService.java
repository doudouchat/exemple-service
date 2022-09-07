package com.exemple.service.customer.subscription;

import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface SubscriptionService {

    void save(@NotBlank String email, @NotNull JsonNode source);

    void save(@NotBlank String email, @NotNull JsonNode source, @NotNull JsonNode previousSource);

    Optional<JsonNode> get(@NotBlank String email);
}
