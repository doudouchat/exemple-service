package com.exemple.service.api.core.authorization.impl;

import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.ForbiddenException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.customer.login.LoginResource;

import lombok.RequiredArgsConstructor;

@Service
@Profile("!noSecurity")
@RequiredArgsConstructor
public class AuthorizationCheckServiceImpl implements AuthorizationCheckService {

    private final LoginResource loginResource;

    @Override
    public void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

        UUID loginId = loginResource.get(securityContext.getUserPrincipal().getName()).orElseThrow(ForbiddenException::new);

        if (!Objects.equals(id, loginId)) {

            throw new ForbiddenException();
        }

    }

    @Override
    public void verifyLogin(String username, ApiSecurityContext securityContext) {

        if (!Objects.equals(username, securityContext.getUserPrincipal().getName())) {

            throw new ForbiddenException();
        }
    }

}
