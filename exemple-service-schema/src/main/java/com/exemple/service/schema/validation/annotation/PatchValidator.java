package com.exemple.service.schema.validation.annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class PatchValidator implements ConstraintValidator<Patch, ArrayNode> {

    @Autowired
    private SchemaValidation schemaValidation;

    @Override
    public boolean isValid(ArrayNode source, ConstraintValidatorContext context) {

        boolean valid = true;

        try {

            schemaValidation.validatePatch(source);

        } catch (ValidationException e) {

            valid = false;

            buildMessageException(e, context);

        }

        return valid;
    }

    private static void buildMessageException(ValidationException exception, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();
        exception.getAllExceptions().stream().forEach((ValidationExceptionModel e) -> context.buildConstraintViolationWithTemplate(e.getMessage())
                .addPropertyNode(e.getPath()).addConstraintViolation());
    }

}
