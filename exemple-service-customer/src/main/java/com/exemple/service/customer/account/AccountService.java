package com.exemple.service.customer.account;

import java.util.Optional;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotNull;

public interface AccountService {

    JsonNode save(@NotNull JsonNode account);

    JsonNode save(@NotNull JsonNode source, @NotNull JsonNode previousSource);

    Optional<JsonNode> get(@NotNull UUID id);

}
