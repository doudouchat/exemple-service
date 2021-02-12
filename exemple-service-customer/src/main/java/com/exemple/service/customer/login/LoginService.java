package com.exemple.service.customer.login;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface LoginService {

    boolean exist(String login);

    void save(@NotNull JsonNode source);

    void save(@NotBlank String login, @NotEmpty @NotNull ArrayNode patch) throws LoginServiceNotFoundException;

    void delete(@NotBlank String login);

    JsonNode get(@NotBlank String login) throws LoginServiceNotFoundException;

    ArrayNode get(@NotNull UUID id) throws LoginServiceNotFoundException;

}
