package com.exemple.service.customer.account.validation.impl;

import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.account.validation.AccountValidationCustom;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class AccountValidationAspect {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final AccountValidationCustom accountValidationCustom;

    public AccountValidationAspect(AccountValidationCustom accountValidationCustom) {
        this.accountValidationCustom = accountValidationCustom;
    }

    @SuppressWarnings("unchecked")
    @After("execution(public void com.exemple.service.customer.account.validation.AccountValidation.validate(*, *, ..)) && args(form, old, ..)")
    public void validate(JsonNode form, JsonNode old) {

        Map<String, Object> formMap = MAPPER.convertValue(form, Map.class);
        Map<String, Object> oldMap = null;
        if (old != null) {
            oldMap = MAPPER.convertValue(old, Map.class);
        }
        accountValidationCustom.validate(formMap, oldMap);
    }

}
