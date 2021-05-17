package com.exemple.service.api.core.authorization.impl;

import java.security.Principal;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.api.core.authorization.AuthorizationContextService;

@Service
@Profile("noSecurity")
public class AuthorizationNoSecureContextServiceImpl implements AuthorizationContextService, AuthorizationCheckService {

    @Override
    public ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) {

        Principal principal = () -> "anonymous";

        return new ApiSecurityContext(principal, "http");
    }

}
