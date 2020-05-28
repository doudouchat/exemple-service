package com.exemple.service.customer.login;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;

public interface LoginService {

    boolean exist(String login);

    void save(@NotNull JsonNode source) throws LoginServiceException;

    void save(@NotBlank String login, @NotNull JsonNode source) throws LoginServiceException;

    void delete(@NotBlank String login);

    JsonNode get(@NotBlank String login) throws LoginServiceNotFoundException;

}
