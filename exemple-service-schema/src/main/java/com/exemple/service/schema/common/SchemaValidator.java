package com.exemple.service.schema.common;

import java.util.function.Consumer;

import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionBuilder;
import com.fasterxml.jackson.databind.JsonNode;

@FunctionalInterface
public interface SchemaValidator extends Consumer<ValidationException> {

    void accept(ValidationException exception);

    static void performValidation(Schema schema, ReadWriteContext context, JsonNode form, SchemaValidator validator) {

        try {
            performValidation(schema, context, form);
        } catch (ValidationException e) {
            validator.accept(e);
        }

    }

    static void performValidation(Schema schema, ReadWriteContext context, JsonNode form) {

        var validator = Validator.builder().readWriteContext(context).build();

        try {

            if (form.isArray()) {
                validator.performValidation(schema, new JSONArray(form.toString()));
            } else {
                validator.performValidation(schema, new JSONObject(form.toString()));
            }
        } catch (org.everit.json.schema.ValidationException e) {

            throw new ValidationException(ValidationExceptionBuilder.buildException(e, form));
        }

    }

}
