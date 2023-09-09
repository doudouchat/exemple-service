package com.exemple.service.schema.common;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.exemple.service.schema.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaValidator {

    public static <T> List<T> performValidation(JsonSchema schema, JsonNode form,
            Function<ValidationException, List<T>> validator) {

        try {
            performValidation(schema, form);
            return Collections.emptyList();
        } catch (ValidationException e) {
            return validator.apply(e);
        }

    }

    public static void performValidation(JsonSchema schema, JsonNode form,
            Consumer<ValidationException> validator) {

        try {
            performValidation(schema, form);
        } catch (ValidationException e) {
            validator.accept(e);
        }

    }

    public static void performValidation(JsonSchema schema, JsonNode form) {

        var validationMessages = schema.validate(form);

        if (!validationMessages.isEmpty()) {

            throw new ValidationException(validationMessages, form);
        }

    }

}
