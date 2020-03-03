package com.exemple.service.api.core.authorization.impl;

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

        JsonNode login = loginResource.get(securityContext.getUserPrincipal().getName()).orElseGet(JsonNodeUtils::init).get(LoginField.ID.field);

        if (!id.toString().equals(login.asText(null))) {

            throw new ForbiddenException();
        }

    }

    @Override
    public void verifyLogin(String login, ApiSecurityContext securityContext) {

        if (!login.equals(securityContext.getUserPrincipal().getName())) {

            throw new ForbiddenException();
        }

    }

}
