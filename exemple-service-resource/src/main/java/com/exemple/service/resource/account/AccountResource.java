package com.exemple.service.resource.account;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.exemple.service.resource.common.validator.Json;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountResource {

    JsonNode save(@NotNull UUID id, @Json(table = "account") JsonNode account);

    JsonNode update(@NotNull UUID id, @NotEmpty @Json(table = "account") JsonNode account);

    Optional<JsonNode> get(UUID id);

    Set<JsonNode> findByIndex(String index, Object value);

}
