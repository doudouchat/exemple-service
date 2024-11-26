package com.exemple.service.schema.validation;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.SchemaValidator;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.CompatibilityFlags;
import com.flipkart.zjsonpatch.JsonPatch;
import com.networknt.schema.JsonSchema;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaValidation {

    private final SchemaBuilder schemaBuilder;

    private final SchemaFilter schemaFilter;

    public void validate(String resource, String version, String profile, JsonNode form) {

        var schema = schemaBuilder.buildCreationValidationSchema(resource, version, profile);
        SchemaValidator.performValidation(schema, form);

    }

    public void validate(String resource, String version, String profile, JsonNode form, JsonNode old) {

        var schema = schemaBuilder.buildUpdateValidationSchema(resource, version, profile);

        SchemaValidator.performValidation(schema, form, (ValidationException e) -> {

            Predicate<ValidationExceptionCause> newExceptionFilter = (
                    ValidationExceptionCause cause) -> !isReadOnlyAndSourceIsNotModified(cause, old);

            throwExceptionIfCausesNotEmpty(e, newExceptionFilter);
        });

    }

    public void validate(String resource, String version, String profile, ArrayNode patch, JsonNode old) {

        var schema = schemaBuilder.buildUpdateValidationSchema(resource, version, profile);

        JsonNode oldFilterBySchema = this.schemaFilter.filterAllProperties(resource, version,profile, old);

        JsonNode form = JsonPatch.apply(patch, oldFilterBySchema,
                EnumSet.of(CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT, CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));

        SchemaValidator.performValidation(schema, form, (ValidationException e) -> {

            Set<ValidationExceptionCause> previousExceptions = findDistinctExceptions(schema, oldFilterBySchema);

            Predicate<ValidationExceptionCause> newExceptionFilter = (ValidationExceptionCause cause) -> isExceptionNotAlreadyExists(cause,
                    previousExceptions);

            throwExceptionIfCausesNotEmpty(e, newExceptionFilter);

        });

    }

    public void validate(JsonSchema schema, JsonNode target) {

        SchemaValidator.performValidation(schema, target);

    }

    private static Set<ValidationExceptionCause> findDistinctExceptions(JsonSchema schema, JsonNode target) {

        Set<ValidationExceptionCause> exceptions = new HashSet<>();
        SchemaValidator.performValidation(schema, target, (ValidationException e) -> e.getCauses().forEach(exceptions::add));
        return exceptions;

    }

    private static void throwExceptionIfCausesNotEmpty(ValidationException e, Predicate<ValidationExceptionCause> filter) {

        var validationException = new ValidationException(e.getCauses().stream().filter(filter).collect(Collectors.toSet()));

        if (!validationException.getCauses().isEmpty()) {
            throw validationException;

        }

    }

    private static boolean isExceptionNotAlreadyExists(ValidationExceptionCause cause, Set<ValidationExceptionCause> previousExceptions) {

        return !previousExceptions.contains(cause);
    }

    private static boolean isReadOnlyAndSourceIsNotModified(ValidationExceptionCause cause, JsonNode previousSource) {

        return "readOnly".equals(cause.getCode()) && previousSource.at(cause.getPath()).equals(cause.getValue());
    }

}
