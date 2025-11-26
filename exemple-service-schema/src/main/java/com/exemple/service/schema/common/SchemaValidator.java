package com.exemple.service.schema.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.fasterxml.jackson.databind.JsonNode;
import com.networknt.schema.JsonSchema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaValidator {

    public static <T, C extends Collection<T>> C performValidation(JsonSchema schema, JsonNode form,
            Function<ValidationException, C> validator, Supplier<C> emptyCollection) {

        try {
            performValidation(schema, form);
            return emptyCollection.get();
        } catch (ValidationException e) {
            return validator.apply(e);
        }

    }

    public static <T> List<T> performValidation(JsonSchema schema, JsonNode form,
            Function<ValidationException, List<T>> validator) {

        return performValidation(schema, form, validator, Collections::emptyList);

    }

    public static void performValidation(JsonSchema schema, JsonNode form,
            Consumer<ValidationException> validator) {

        try {
            performValidation(schema, form);
        } catch (ValidationException e) {
            validator.accept(e);
        }

    }

    public static Set<ValidationExceptionCause> findValidationExceptionCauses(JsonSchema schema, JsonNode form) {

        return performValidation(schema, form, ValidationException::getCauses, Set::of);
    }

    public static void performValidation(JsonSchema schema, JsonNode form) {

        var validationMessages = schema.validate(form);

        if (!validationMessages.isEmpty()) {

            throw new ValidationException(validationMessages, form);
        }

    }

}
