package com.exemple.service.api.common.script;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.customer.account.AccountService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class AccountServiceSupplier implements Supplier<AccountService> {

    private static final String ACCOUNT_BEAN = "accountService";

    @Autowired
    private CustomerScriptFactory scriptFactory;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public AccountService get() {
        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        return scriptFactory.getBean(ACCOUNT_BEAN, AccountService.class, app);

    }

}
