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
import com.fasterxml.jackson.databind.node.JsonNodeType;

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

        if (!id.toString().equals(login.get(LoginField.ID.field).asText(null))) {

            throw new ForbiddenException();
        }

    }

    @Override
    public void verifyLogin(String username, ApiSecurityContext securityContext) {

        if (!username.equals(securityContext.getUserPrincipal().getName())) {

            JsonNode login = loginResource.get(username).orElseGet(JsonNodeUtils::init);

            if (JsonNodeType.MISSING == login.path(LoginField.ID.field).getNodeType()) {

                throw new ForbiddenException();

            }

            UUID id = UUID.fromString(login.get(LoginField.ID.field).textValue());
            if (loginResource.get(id).stream().map(l -> l.get(LoginField.USERNAME.field).textValue())
                    .noneMatch(securityContext.getUserPrincipal().getName()::equals)) {

                throw new ForbiddenException();

            }
        }
    }

}
