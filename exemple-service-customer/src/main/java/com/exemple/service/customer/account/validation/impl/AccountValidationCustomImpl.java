package com.exemple.service.customer.account.validation.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.ValidationCustom;
import com.exemple.service.customer.core.script.CustomerScriptFactory;

@Component
public class AccountValidationCustomImpl implements ValidationCustom {

    private static final String BEAN_NAME = "accountValidationCustom";

    private final CustomerScriptFactory scriptFactory;

    public AccountValidationCustomImpl(CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public void validate(Map<String, Object> form, Map<String, Object> old) {
        scriptFactory.getBean(BEAN_NAME, ValidationCustom.class).validate(form, old);
    }

}
