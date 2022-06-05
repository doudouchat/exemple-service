package com.exemple.service.customer.subscription;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionService {

    void save(@NotBlank String email, @NotNull JsonNode source);
    
    void save(@NotBlank String email, @NotNull JsonNode source, @NotNull JsonNode previousSource);

    Optional<JsonNode> get(@NotBlank String email);
}
