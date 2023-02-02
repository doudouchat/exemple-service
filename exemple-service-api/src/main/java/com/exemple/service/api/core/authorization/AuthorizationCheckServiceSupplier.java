package com.exemple.service.api.core.authorization;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.LoginService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class AuthorizationCheckServiceSupplier implements Supplier<AuthorizationCheckService> {

    @Context
    private LoginService loginService;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public AuthorizationCheckService get() {
        return new AuthorizationCheckService(loginService, requestContext);
    }

}
