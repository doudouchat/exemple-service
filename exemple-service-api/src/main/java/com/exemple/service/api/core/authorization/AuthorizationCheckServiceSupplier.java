package com.exemple.service.api.core.authorization;

import java.util.function.Supplier;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.LoginResource;

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
