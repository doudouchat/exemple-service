package com.exemple.service.api.core.authorization.impl;

import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.ForbiddenException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.model.LoginEntity;

@Service
@Profile("!noSecurity")
public class AuthorizationCheckServiceImpl implements AuthorizationCheckService {

    private final LoginResource loginResource;

    public AuthorizationCheckServiceImpl(LoginResource loginResource) {

        this.loginResource = loginResource;
    }

    @Override
    public void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

        LoginEntity login = loginResource.get(securityContext.getUserPrincipal().getName()).orElseThrow(ForbiddenException::new);

        checkIfIdAreIdentical(id, login.getId());

    }

    @Override
    public void verifyLogin(String username, ApiSecurityContext securityContext) {

        if (!username.equals(securityContext.getUserPrincipal().getName())) {

            LoginEntity login1 = loginResource.get(username).orElseThrow(ForbiddenException::new);

            LoginEntity login2 = loginResource.get(securityContext.getUserPrincipal().getName()).orElseThrow(ForbiddenException::new);

            checkIfIdAreIdentical(login1.getId(), login2.getId());

        }
    }

    private static void checkIfIdAreIdentical(UUID id1, UUID id2) {

        if (!Objects.equals(id1, id2)) {

            throw new ForbiddenException();
        }
    }

}
