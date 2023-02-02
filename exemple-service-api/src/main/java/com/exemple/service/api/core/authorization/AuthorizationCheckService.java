package com.exemple.service.api.core.authorization;

import java.util.Objects;
import java.util.UUID;

import com.exemple.service.customer.login.LoginService;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthorizationCheckService {

    private final LoginService loginService;

    private final ContainerRequestContext requestContext;

    public void verifyAccountId(UUID id) {

        UUID loginId = loginService.get(requestContext.getSecurityContext().getUserPrincipal().getName()).orElseThrow(ForbiddenException::new);

        if (!Objects.equals(id, loginId)) {

            throw new ForbiddenException();
        }

    }

    public void verifyLogin(String username) {

        if (!Objects.equals(username, requestContext.getSecurityContext().getUserPrincipal().getName())) {

            throw new ForbiddenException();
        }
    }
}
