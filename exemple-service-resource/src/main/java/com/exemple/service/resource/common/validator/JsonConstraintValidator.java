package com.exemple.service.resource.common.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exemple.service.customer.common.validator.Json;
import com.exemple.service.resource.common.JsonValidatorException;
import com.exemple.service.resource.common.validator.json.JsonValidator;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class JsonConstraintValidator implements ConstraintValidator<Json, JsonNode> {

    private static final Logger LOG = LoggerFactory.getLogger(JsonConstraintValidator.class);

    private String table;

    private String messageTemplate;

    private final JsonValidator jsonValidator;

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        boolean valid = true;

        try {

            jsonValidator.valid(source, table);

        } catch (JsonValidatorException e) {

            valid = false;

            LOG.trace(e.getMessage(messageTemplate), e);

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage(this.messageTemplate)).addPropertyNode(e.getNode()).addConstraintViolation();
        }

        return valid;
    }

    @Override
    public void initialize(Json constraintAnnotation) {

        this.table = constraintAnnotation.table();
        this.messageTemplate = constraintAnnotation.message();

    }

}
