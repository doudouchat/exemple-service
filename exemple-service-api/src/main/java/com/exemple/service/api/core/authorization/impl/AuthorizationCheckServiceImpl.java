package com.exemple.service.api.core.authorization.impl;

import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.container.ContainerRequestContext;

import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.customer.login.LoginResource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthorizationCheckServiceImpl implements AuthorizationCheckService {

    private final LoginResource loginResource;

    private final ContainerRequestContext requestContext;

    @Override
    public void verifyAccountId(UUID id) {

        UUID loginId = loginResource.get(requestContext.getSecurityContext().getUserPrincipal().getName()).orElseThrow(ForbiddenException::new);

        if (!Objects.equals(id, loginId)) {

            throw new ForbiddenException();
        }

    }

    @Override
    public void verifyLogin(String username) {

        if (!Objects.equals(username, requestContext.getSecurityContext().getUserPrincipal().getName())) {

            throw new ForbiddenException();
        }
    }

}
