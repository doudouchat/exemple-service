package com.exemple.service.resource.login;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.resource.common.validator.Json;
import com.exemple.service.resource.core.statement.LoginStatement;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginResource {

    Optional<JsonNode> get(@NotBlank String login);

    void save(@NotBlank String login, @NotNull @Json(table = LoginStatement.LOGIN) JsonNode source);

    void save(@NotNull @Json(table = LoginStatement.LOGIN) JsonNode source) throws LoginResourceExistException;

    void delete(@NotBlank String login);
}
