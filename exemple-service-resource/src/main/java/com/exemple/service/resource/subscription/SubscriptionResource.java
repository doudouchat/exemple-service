package com.exemple.service.resource.subscription;

import java.util.Optional;

import javax.validation.constraints.NotBlank;

import com.exemple.service.resource.common.validator.Json;
import com.fasterxml.jackson.databind.JsonNode;

public interface SubscriptionResource {

    Optional<JsonNode> get(@NotBlank String email);

    void save(@Json(table = "subscription") JsonNode source);

    void delete(String email);
}
