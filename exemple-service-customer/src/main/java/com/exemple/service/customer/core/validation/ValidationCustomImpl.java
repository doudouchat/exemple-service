package com.exemple.service.customer.core.validation;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.ValidationCustom;
import com.exemple.service.customer.core.script.CustomerScriptFactory;

@Component
public class ValidationCustomImpl implements ValidationCustom {

    private static final String BEAN_NAME = "ValidationCustom";

    private final CustomerScriptFactory scriptFactory;

    public ValidationCustomImpl(CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public void validate(Map<String, Object> form, Map<String, Object> old) {
        scriptFactory.getBean(ValidationCustomContext.context().getResource() + BEAN_NAME, ValidationCustom.class).validate(form, old);
    }

    @Override
    public void validate(Map<String, Object> form) {
        scriptFactory.getBean(ValidationCustomContext.context().getResource() + BEAN_NAME, ValidationCustom.class).validate(form);

    }

}
