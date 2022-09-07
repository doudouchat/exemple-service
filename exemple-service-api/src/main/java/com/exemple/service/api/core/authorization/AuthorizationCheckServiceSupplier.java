package com.exemple.service.api.core.authorization;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.LoginResource;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class AuthorizationCheckServiceSupplier implements Supplier<AuthorizationCheckService> {

    @Autowired
    private LoginResource loginResource;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public AuthorizationCheckService get() {
        return new AuthorizationCheckService(loginResource, requestContext);
    }

}
