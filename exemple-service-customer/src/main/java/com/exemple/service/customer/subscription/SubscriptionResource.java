package com.exemple.service.customer.subscription;

import java.util.Optional;

import com.exemple.service.customer.common.validator.Json;
import com.exemple.service.customer.common.validator.NotEmpty;

import jakarta.validation.constraints.NotBlank;
import tools.jackson.databind.JsonNode;

public interface SubscriptionResource {

    Optional<JsonNode> get(@NotBlank String email);

    void create(@Json(table = "subscription") JsonNode source);

    void update(@NotEmpty @Json(table = "subscription") JsonNode subscription);

    void delete(String email);
}
