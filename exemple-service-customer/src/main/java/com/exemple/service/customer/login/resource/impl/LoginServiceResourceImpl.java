package com.exemple.service.customer.login.resource.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.core.script.CustomerScriptFactory;
import com.exemple.service.customer.login.resource.LoginServiceResource;

@Component
public class LoginServiceResourceImpl implements LoginServiceResource {

    private static final String BEAN_NAME = "loginServiceResource";

    private final CustomerScriptFactory scriptFactory;

    public LoginServiceResourceImpl(CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public Map<String, Object> saveLogin(Map<String, Object> source) {
        return scriptFactory.getBean(BEAN_NAME, LoginServiceResource.class).saveLogin(source);
    }

}
