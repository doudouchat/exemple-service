package com.exemple.service.customer.core.validator.rule;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.LoginService;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationException.ValidationExceptionModel;
import com.exemple.service.schema.core.validator.ValidatorService;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class LoginValidator implements ValidatorService {

    private final LoginService loginService;

    public LoginValidator(LoginService loginService) {

        this.loginService = loginService;
    }

    @Override
    public void validate(String path, JsonNode form, JsonNode old, ValidationException validationException) {

        JsonNode unique = form.at(path);

        if (unique.isTextual() && !validationException.contains(path) && loginService.exist(unique.textValue())) {

            ValidationExceptionModel exception = new ValidationExceptionModel(path, "login",
                    "[".concat(unique.textValue()).concat("] already exists"));

            validationException.add(exception);

        }

    }

}
