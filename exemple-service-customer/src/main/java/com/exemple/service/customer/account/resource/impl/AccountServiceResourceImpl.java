package com.exemple.service.customer.account.resource.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.account.resource.AccountServiceResource;
import com.exemple.service.customer.core.script.CustomerScriptFactory;

@Component
public class AccountServiceResourceImpl implements AccountServiceResource {

    private static final String BEAN_NAME = "accountServiceResource";

    private final CustomerScriptFactory scriptFactory;

    public AccountServiceResourceImpl(CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public Map<String, Object> save(Map<String, Object> account) {
        return scriptFactory.getBean(BEAN_NAME, AccountServiceResource.class).save(account);
    }

    @Override
    public Map<String, Object> update(Map<String, Object> account) {
        return scriptFactory.getBean(BEAN_NAME, AccountServiceResource.class).update(account);

    }

}
