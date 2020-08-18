package com.exemple.service.customer.account;

import java.util.UUID;

import javax.validation.constraints.NotNull;

import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface AccountService {

    JsonNode save(@NotNull JsonNode account) throws AccountServiceException;

    JsonNode save(@NotNull UUID id, @NotEmpty @NotNull ArrayNode patch) throws AccountServiceException;

    JsonNode get(@NotNull UUID id) throws AccountServiceException;

}
