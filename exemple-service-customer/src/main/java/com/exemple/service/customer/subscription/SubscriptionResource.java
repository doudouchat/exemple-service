package com.exemple.service.customer.subscription;

import java.util.Optional;

import javax.validation.constraints.NotBlank;

import com.exemple.service.customer.common.validator.Json;
import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionResource {

    Optional<JsonNode> get(@NotBlank String email);

    void save(@Json(table = "subscription") JsonNode source);

    void delete(String email);
}
