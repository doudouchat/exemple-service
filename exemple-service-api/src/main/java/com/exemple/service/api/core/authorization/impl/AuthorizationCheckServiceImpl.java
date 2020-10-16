package com.exemple.service.api.core.authorization.impl;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.ForbiddenException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Profile("!noSecurity")
public class AuthorizationCheckServiceImpl implements AuthorizationCheckService {

    private final LoginResource loginResource;

    public AuthorizationCheckServiceImpl(LoginResource loginResource) {

        this.loginResource = loginResource;
    }

    @Override
    public void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

        JsonNode login = loginResource.get(securityContext.getUserPrincipal().getName()).orElseGet(JsonNodeUtils::init);

        checkIfAccountIdAndLoginIdAreIdentical(id, login);

    }

    @Override
    public void verifyLogin(String username, ApiSecurityContext securityContext) {

        if (!username.equals(securityContext.getUserPrincipal().getName())) {

            JsonNode login = loginResource.get(username).orElseGet(JsonNodeUtils::init);

            checkIfLoginIdIsPresent(login);

            UUID id = UUID.fromString(login.get(LoginField.ID.field).textValue());
            checkIfUsernameExistInLogin(securityContext.getUserPrincipal().getName(), loginResource.get(id));
        }
    }

    private static void checkIfAccountIdAndLoginIdAreIdentical(UUID id, JsonNode login) {

        if (!id.toString().equals(login.path(LoginField.ID.field).asText(null))) {

            throw new ForbiddenException();
        }
    }

    private static void checkIfLoginIdIsPresent(JsonNode login) {

        if (login.path(LoginField.ID.field).isMissingNode()) {

            throw new ForbiddenException();

        }
    }

    private static void checkIfUsernameExistInLogin(String username, List<JsonNode> logins) {

        if (logins.stream().map(l -> l.get(LoginField.USERNAME.field).textValue()).noneMatch(username::equals)) {

            throw new ForbiddenException();

        }
    }

}
