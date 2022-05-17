package com.exemple.service.api.common.script;

import java.util.function.Supplier;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.customer.account.AccountService;

@Component
public class AccountServiceSupplier implements Supplier<AccountService> {

    private static final String ACCOUNT_BEAN = "accountService";

    @Autowired
    private CustomerScriptFactory scriptFactory;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public AccountService get() {
        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        return scriptFactory.getBean(ACCOUNT_BEAN, AccountService.class, app);

    }

}
