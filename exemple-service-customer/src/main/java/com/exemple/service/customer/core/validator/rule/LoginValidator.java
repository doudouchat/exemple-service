package com.exemple.service.customer.core.validator.rule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.LoginService;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationException.ValidationExceptionModel;
import com.exemple.service.schema.core.validator.ValidatorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Component
public class LoginValidator implements ValidatorService {

    @Autowired
    private LoginService loginService;

    @Override
    public void validate(String path, JsonNode form, JsonNode old, ValidationException validationException) {

        JsonNode unique = form.at(path);

        if (JsonNodeType.STRING == unique.getNodeType() && !validationException.contains(path)) {

            JsonNode oldUnique = null;
            if (old != null) {
                oldUnique = old.at(path);
            }

            if ((oldUnique == null || !unique.equals(oldUnique)) && loginService.exist(unique.textValue())) {

                ValidationExceptionModel exception = new ValidationExceptionModel(path, "login",
                        "[".concat(unique.textValue()).concat("] already exists"));

                validationException.add(exception);

            }
        }

    }

}
