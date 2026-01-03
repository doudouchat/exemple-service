package com.exemple.service.resource.common.validator;

import com.exemple.service.customer.common.validator.NotEmpty;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tools.jackson.databind.JsonNode;

public class NotEmptyConstraintValidator implements ConstraintValidator<NotEmpty, JsonNode> {

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        return !source.isEmpty();
    }

}
