package com.exemple.service.customer.login.validation.impl;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.TransformUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class LoginValidationAspect {

    private final LoginValidationCustomImpl loginValidationCustom;

    public LoginValidationAspect(LoginValidationCustomImpl loginValidationCustom) {
        this.loginValidationCustom = loginValidationCustom;
    }

    @After("execution(public void com.exemple.service.customer.login.validation.LoginValidation.validate(*, *, ..)) && args(form, old, ..)")
    public void validate(JsonNode form, JsonNode old) {

        TransformUtils.transform(form, old, loginValidationCustom::validate);
    }

}
