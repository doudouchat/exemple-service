package com.exemple.service.resource.login;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.resource.common.validator.Json;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginResource {

    Optional<JsonNode> get(@NotBlank String username);

    void save(@NotBlank String username, @NotEmpty @Json(table = "login") JsonNode source) throws LoginResourceExistException;

    void save(@Json(table = "login") JsonNode source) throws LoginResourceExistException;

    void delete(@NotBlank String username);

    List<JsonNode> get(@NotNull UUID id);
}
