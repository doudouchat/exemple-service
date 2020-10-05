package com.exemple.service.customer.common.script.validation;

import java.util.Map;

import com.exemple.service.customer.common.CustomerScriptFactory;
import com.exemple.service.customer.common.script.CustomiseValidation;

public class CustomiseValidationImpl implements CustomiseValidation {

    private final String beanName;

    private final CustomerScriptFactory scriptFactory;

    public CustomiseValidationImpl(String beanName, CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
        this.beanName = beanName;
    }

    @Override
    public void validate(Map<String, Object> form, Map<String, Object> old) {
        scriptFactory.getBean(beanName, CustomiseValidation.class).validate(form, old);
    }

    @Override
    public void validate(Map<String, Object> form) {
        scriptFactory.getBean(beanName, CustomiseValidation.class).validate(form);

    }

}
