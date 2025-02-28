package com.exemple.service.customer.account;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public interface AccountService {

    JsonNode create(@NotNull JsonNode account);

    JsonNode update(@NotNull JsonNode account);

    Optional<JsonNode> get(@NotNull UUID id);

}
