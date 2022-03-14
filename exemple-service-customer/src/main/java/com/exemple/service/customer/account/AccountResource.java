package com.exemple.service.customer.account;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.exemple.service.customer.common.validator.Json;
import com.exemple.service.customer.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountResource {

    UUID save(@NotEmpty @Json(table = "account") JsonNode account);

    void save(@NotEmpty @Json(table = "account") JsonNode account, @NotEmpty JsonNode previousAccount);

    Optional<JsonNode> get(UUID id);

    Set<JsonNode> findByIndex(String index, Object value);

}
