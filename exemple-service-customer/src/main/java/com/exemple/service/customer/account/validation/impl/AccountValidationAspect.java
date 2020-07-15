package com.exemple.service.customer.account.validation.impl;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.TransformUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class AccountValidationAspect {

    private final AccountValidationCustomImpl accountValidationCustom;

    public AccountValidationAspect(AccountValidationCustomImpl accountValidationCustom) {
        this.accountValidationCustom = accountValidationCustom;
    }

    @After("execution(public void com.exemple.service.customer.account.validation.AccountValidation.validate(*, *, ..)) && args(form, old, ..)")
    public void validate(JsonNode form, JsonNode old) {

        TransformUtils.transform(form, old, accountValidationCustom::validate);

    }

}
