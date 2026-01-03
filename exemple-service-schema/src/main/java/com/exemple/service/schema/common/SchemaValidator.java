package com.exemple.service.schema.common;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.networknt.schema.ExecutionContext;
import com.networknt.schema.Schema;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaValidator {

    public static <T, C extends Collection<T>> C performValidation(Schema schema, JsonNode form,
            Function<ValidationException, C> validator, Supplier<C> emptyCollection, boolean checkOnlyWrite) {

        try {
            performValidation(schema, form, checkOnlyWrite);
            return emptyCollection.get();
        } catch (ValidationException e) {
            return validator.apply(e);
        }

    }

    public static <T> List<T> performValidation(Schema schema, JsonNode form,
            Function<ValidationException, List<T>> validator, boolean checkOnlyWrite) {

        return performValidation(schema, form, validator, Collections::emptyList, checkOnlyWrite);

    }

    public static void performValidation(Schema schema, JsonNode form,
            Consumer<ValidationException> validator) {

        try {
            performValidation(schema, form, false);
        } catch (ValidationException e) {
            validator.accept(e);
        }

    }

    public static List<ValidationExceptionCause> findValidationExceptionCauses(Schema schema, JsonNode form) {

        return performValidation(schema, form, ValidationException::getCauses, List::of, false);
    }

    public static void performValidation(Schema schema, JsonNode form) {

        performValidation(schema, form, false);

    }

    public static void performValidation(Schema schema, JsonNode form, boolean checkOnlyWrite) {

        var validationMessages = schema.validate(form, (ExecutionContext executionContext) -> executionContext
                .executionConfig(executionConfig -> executionConfig
                        .readOnly(true)
                        .formatAssertionsEnabled(true)
                        .writeOnly(checkOnlyWrite)));

        if (!validationMessages.isEmpty()) {

            throw new ValidationException(validationMessages, form);
        }

    }

}
