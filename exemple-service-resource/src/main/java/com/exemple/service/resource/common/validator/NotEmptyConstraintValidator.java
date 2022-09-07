package com.exemple.service.resource.common.validator;

import com.exemple.service.customer.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NotEmptyConstraintValidator implements ConstraintValidator<NotEmpty, JsonNode> {

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        return !source.isEmpty();
    }

}
