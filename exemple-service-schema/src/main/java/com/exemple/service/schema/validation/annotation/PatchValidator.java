package com.exemple.service.schema.validation.annotation;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.node.ArrayNode;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PatchValidator implements ConstraintValidator<Patch, ArrayNode> {

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private SchemaBuilder patchSchema;

    @Override
    public boolean isValid(ArrayNode source, ConstraintValidatorContext context) {

        var valid = true;

        try {

            schemaValidation.validate(patchSchema.buildPatchSchema(), source);

        } catch (ValidationException e) {

            valid = false;

            buildMessageException(e, context);

        }

        return valid;
    }

    private static void buildMessageException(ValidationException exception, ConstraintValidatorContext context) {

        context.disableDefaultConstraintViolation();
        exception.getCauses().stream().forEach((ValidationExceptionCause e) -> context.buildConstraintViolationWithTemplate(e.getMessage())
                .addPropertyNode(e.getPath()).addConstraintViolation());
    }

}
