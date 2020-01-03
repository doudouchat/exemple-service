package com.exemple.service.api.core.authorization.impl;

import java.security.Principal;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationContextService;

@Service
@Profile("noSecurity")
public class AuthorizationNoSecureContextServiceImpl implements AuthorizationContextService {

    @Override
    public ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) {

        Principal principal = () -> "anonymous";

        return new ApiSecurityContext(principal, "http");
    }

    @Override
    public void cleanContext(ApiSecurityContext securityContext, Response.StatusType statusInfo) {

        // Nope

    }

    @Override
    public void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

        // Nope

    }

    @Override
    public void verifyLogin(String login, ApiSecurityContext securityContext) {

        // Nope

    }

}
