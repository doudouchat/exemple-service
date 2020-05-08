package com.exemple.service.schema.core.validator.rule;

import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationException.ValidationExceptionModel;
import com.exemple.service.schema.core.validator.ValidatorService;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class CreateOnlyValidator implements ValidatorService {

    @Override
    public void validate(String path, JsonNode form, JsonNode old, ValidationException validationException) {

        JsonNode createOnly = form.at(path);

        if (!createOnly.isMissingNode() && old != null && !createOnly.equals(old.at(path))) {

            ValidationExceptionModel exception = new ValidationExceptionModel(path, "createOnly", "[".concat(path).concat("] is already created"));

            validationException.add(exception);

        }

    }

}
