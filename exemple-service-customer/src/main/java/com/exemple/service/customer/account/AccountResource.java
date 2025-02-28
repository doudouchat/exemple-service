package com.exemple.service.customer.account;

import java.util.Optional;
import java.util.UUID;

import com.exemple.service.customer.common.validator.Json;
import com.exemple.service.customer.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;

public interface AccountResource {

    void create(@NotEmpty @Json(table = "account") JsonNode account);

    void update(@NotEmpty @Json(table = "account") JsonNode account);

    Optional<JsonNode> get(UUID id);

    Optional<UUID> getIdByUsername(@NotBlank String field, @NotBlank String value);

    void removeByUsername(@NotBlank String field, @NotBlank String value);

}
