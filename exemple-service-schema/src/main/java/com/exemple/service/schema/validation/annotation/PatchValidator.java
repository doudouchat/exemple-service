package com.exemple.service.schema.validation.annotation;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.validation.SchemaValidation;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import tools.jackson.databind.node.ArrayNode;

public class PatchValidator implements ConstraintValidator<Patch, ArrayNode> {

    private final SchemaValidation schemaValidation;

    private final SchemaBuilder patchSchema;

    @Inject
    public PatchValidator(SchemaValidation schemaValidation, SchemaBuilder patchSchema) {
        this.schemaValidation = schemaValidation;
        this.patchSchema = patchSchema;
    }

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
