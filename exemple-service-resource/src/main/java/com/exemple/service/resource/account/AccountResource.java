package com.exemple.service.resource.account;

import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.exemple.service.resource.common.validator.Json;
import com.exemple.service.resource.core.statement.AccountStatement;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountResource {

    JsonNode save(@NotNull UUID id, @NotNull @Json(table = AccountStatement.TABLE) JsonNode account);

    JsonNode update(@NotNull UUID id, @NotNull @Json(table = AccountStatement.TABLE) JsonNode account);

    Optional<JsonNode> get(UUID id);

    JsonNode getByStatus(String status);

}
