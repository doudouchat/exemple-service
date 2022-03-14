package com.exemple.service.resource.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.exemple.service.customer.common.validator.NotEmpty;
import com.fasterxml.jackson.databind.JsonNode;

public class NotEmptyConstraintValidator implements ConstraintValidator<NotEmpty, JsonNode> {

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        return !source.isEmpty();
    }

}
