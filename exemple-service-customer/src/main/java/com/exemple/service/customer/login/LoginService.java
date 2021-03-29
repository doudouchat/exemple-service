package com.exemple.service.customer.login;

import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface LoginService {

    boolean exist(String username);

    void save(@NotNull JsonNode source) throws LoginServiceAlreadyExistException;

    void save(@NotNull JsonNode source, @NotNull JsonNode previousSource);

    void save(@NotEmpty ArrayNode source, @NotEmpty ArrayNode previousSource) throws LoginServiceAlreadyExistException;

    void delete(@NotBlank String username);

    JsonNode get(@NotBlank String username) throws LoginServiceNotFoundException;

    ArrayNode get(@NotNull UUID id) throws LoginServiceNotFoundException;

}
