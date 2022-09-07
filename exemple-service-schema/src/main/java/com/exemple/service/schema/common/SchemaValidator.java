package com.exemple.service.schema.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.json.JSONArray;
import org.json.JSONObject;

import com.exemple.service.schema.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaValidator {

    public static <T> List<T> performValidation(Schema schema, ReadWriteContext context, JsonNode form,
            Function<ValidationException, List<T>> validator) {

        try {
            performValidation(schema, context, form);
            return Collections.emptyList();
        } catch (ValidationException e) {
            return validator.apply(e);
        }

    }

    public static void performValidation(Schema schema, ReadWriteContext context, JsonNode form,
            Consumer<ValidationException> validator) {

        try {
            performValidation(schema, context, form);
        } catch (ValidationException e) {
            validator.accept(e);
        }

    }

    public static void performValidation(Schema schema, ReadWriteContext context, JsonNode form) {

        var validator = Validator.builder().readWriteContext(context).build();

        try {

            if (form.isArray()) {
                validator.performValidation(schema, new JSONArray(form.toString()));
            } else {
                validator.performValidation(schema, new JSONObject(form.toString()));
            }
        } catch (org.everit.json.schema.ValidationException e) {

            throw new ValidationException(e, form);
        }

    }

}
