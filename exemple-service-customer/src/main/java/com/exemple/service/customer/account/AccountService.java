package com.exemple.service.customer.account;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountService {

    JsonNode save(@NotNull JsonNode account);

    JsonNode save(@NotNull JsonNode source, @NotNull JsonNode previousSource);

    JsonNode get(@NotNull UUID id) throws AccountServiceNotFoundException;

}
