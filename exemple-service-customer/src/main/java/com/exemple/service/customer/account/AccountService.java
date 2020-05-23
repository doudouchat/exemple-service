package com.exemple.service.customer.account;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.account.exception.AccountServiceException;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountService {

    JsonNode save(@NotNull JsonNode account) throws AccountServiceException;

    JsonNode save(@NotNull UUID id, @Valid @NotNull JsonNode account) throws AccountServiceException;

    JsonNode get(@NotNull UUID id) throws AccountServiceException;

}
