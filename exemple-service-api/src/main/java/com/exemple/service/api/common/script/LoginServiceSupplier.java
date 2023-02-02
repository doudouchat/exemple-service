package com.exemple.service.api.common.script;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.customer.login.LoginService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class LoginServiceSupplier implements Supplier<LoginService> {

    private static final String LOGIN_BEAN = "loginService";

    @Autowired
    private CustomerScriptFactory scriptFactory;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public LoginService get() {
        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        return scriptFactory.getBean(LOGIN_BEAN, LoginService.class, app);

    }

}
