package com.exemple.service.api.common.script;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.customer.account.AccountService;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class AccountServiceSupplier implements Supplier<AccountService> {

    private static final String ACCOUNT_BEAN = "accountService";

    private final CustomerScriptFactory scriptFactory;

    @Context
    private ContainerRequestContext requestContext;

    @Inject
    public AccountServiceSupplier(CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public AccountService get() {
        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        return scriptFactory.getBean(ACCOUNT_BEAN, AccountService.class, app);

    }

}
