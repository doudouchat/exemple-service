package com.exemple.service.resource.common.validator;

import com.exemple.service.customer.common.validator.Json;
import com.exemple.service.resource.common.JsonValidatorException;
import com.exemple.service.resource.common.validator.json.JsonValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
@Slf4j
public class JsonConstraintValidator implements ConstraintValidator<Json, JsonNode> {

    private String table;

    private String messageTemplate;

    private final JsonValidator jsonValidator;

    @Override
    public boolean isValid(JsonNode source, ConstraintValidatorContext context) {

        var valid = true;

        try {

            jsonValidator.valid(source, table);

        } catch (JsonValidatorException e) {

            valid = false;

            var message = new StringBuilder();
            message.append(messageTemplate.replace("{", "").replace("}", "")).append('.').append(e.getKey());

            LOG.trace(message.toString(), e);

            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message.toString()).addPropertyNode(e.getNode()).addConstraintViolation();
        }

        return valid;
    }

    @Override
    public void initialize(Json constraintAnnotation) {

        this.table = constraintAnnotation.table();
        this.messageTemplate = constraintAnnotation.message();

    }

}
