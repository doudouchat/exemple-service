package com.exemple.service.customer.login;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginService {

    boolean exist(String username);

    void save(@NotNull JsonNode source) throws LoginServiceAlreadyExistException;

    void save(@NotBlank String username, @NotNull JsonNode source, @NotNull JsonNode previousSource) throws LoginServiceAlreadyExistException;

    void delete(@NotBlank String username);

    JsonNode get(@NotBlank String username) throws LoginServiceNotFoundException;

    List<JsonNode> get(@NotNull UUID id) throws LoginServiceNotFoundException;

}
