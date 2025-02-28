package com.exemple.service.customer.subscription;

import java.util.Optional;

import com.exemple.service.customer.common.validator.Json;
import com.exemple.service.customer.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;

public interface SubscriptionResource {

    Optional<JsonNode> get(@NotBlank String email);

    void create(@Json(table = "subscription") JsonNode source);

    void update(@NotEmpty @Json(table = "subscription") JsonNode subscription);

    void delete(String email);
}
