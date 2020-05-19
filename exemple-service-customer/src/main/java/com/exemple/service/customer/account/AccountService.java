package com.exemple.service.customer.account;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.account.exception.AccountServiceException;
import com.fasterxml.jackson.databind.JsonNode;

public interface AccountService {

    JsonNode save(@NotNull JsonNode account, @NotBlank String app, @NotBlank String version, @NotBlank String profile) throws AccountServiceException;

    JsonNode save(@NotNull UUID id, @NotNull JsonNode account, @NotBlank String app, @NotBlank String version, @NotBlank String profile)
            throws AccountServiceException;

    JsonNode get(@NotNull UUID id, @NotBlank String app, @NotBlank String version, @NotBlank String profile) throws AccountServiceException;

}
