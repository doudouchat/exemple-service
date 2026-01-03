package com.exemple.service.customer.account;

import java.util.Optional;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public interface AccountService {

    JsonNode create(@NotNull JsonNode account);

    JsonNode update(@NotNull JsonNode account);

    Optional<JsonNode> get(@NotNull UUID id);

}
