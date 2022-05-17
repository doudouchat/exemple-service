package com.exemple.service.api.core.authorization.impl;

import java.util.function.Supplier;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import com.exemple.service.api.core.authorization.AuthorizationCheckService;
import com.exemple.service.customer.login.LoginResource;

@Component
@Profile("!noSecurity")
public class AuthorizationCheckServiceSupplier implements Supplier<AuthorizationCheckService> {

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private ConfigurableEnvironment env;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public AuthorizationCheckService get() {
        if (ArrayUtils.contains(env.getActiveProfiles(), "noSecurity")) {
            return new AuthorizationCheckService() {
            };
        }
        return new AuthorizationCheckServiceImpl(loginResource, requestContext);
    }

}
