package com.exemple.service.schema.validation.impl;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.SchemaValidator;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.CompatibilityFlags;
import com.flipkart.zjsonpatch.JsonPatch;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaValidationImpl implements SchemaValidation {

    private final SchemaBuilder schemaBuilder;

    private final SchemaFilter schemaFilter;

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form) {

        Schema schema = schemaBuilder.buildCreationSchema(app, version, resource, profile);
        SchemaValidator.performValidation(schema, ReadWriteContext.WRITE, form);

    }

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form, JsonNode old) {

        Schema schema = schemaBuilder.buildUpdateSchema(app, version, resource, profile);

        SchemaValidator.performValidation(schema, ReadWriteContext.WRITE, form, (ValidationException e) -> {

            Predicate<ValidationExceptionCause> newExceptionFilter = (
                    ValidationExceptionCause cause) -> !isReadOnlyAndSourceIsNotModified(cause, old);

            throwExceptionIfCausesNotEmpty(e, newExceptionFilter);
        });

    }

    @Override
    public void validate(String app, String version, String profile, String resource, ArrayNode patch, JsonNode old) {

        Schema schema = schemaBuilder.buildUpdateSchema(app, version, resource, profile);

        JsonNode oldFilterBySchema = this.schemaFilter.filterAllProperties(app, version, resource, profile, old);

        JsonNode form = JsonPatch.apply(patch, oldFilterBySchema,
                EnumSet.of(CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT, CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));

        SchemaValidator.performValidation(schema, ReadWriteContext.WRITE, form, (ValidationException e) -> {

            Set<ValidationExceptionCause> previousExceptions = findDistinctExceptions(schema, oldFilterBySchema);

            Predicate<ValidationExceptionCause> newExceptionFilter = (ValidationExceptionCause cause) -> isExceptionNotAlreadyExists(cause,
                    previousExceptions);

            throwExceptionIfCausesNotEmpty(e, newExceptionFilter);

        });

    }

    @Override
    public void validate(Schema schema, JsonNode target) {

        SchemaValidator.performValidation(schema, ReadWriteContext.WRITE, target);

    }

    private static Set<ValidationExceptionCause> findDistinctExceptions(Schema schema, JsonNode target) {

        Set<ValidationExceptionCause> exceptions = new HashSet<>();
        SchemaValidator.performValidation(schema, ReadWriteContext.WRITE, target, (ValidationException e) -> e.getCauses().forEach(exceptions::add));
        return exceptions;

    }

    private static void throwExceptionIfCausesNotEmpty(ValidationException e, Predicate<ValidationExceptionCause> filter) {

        ValidationException validationException = new ValidationException(
                e.getCauses().stream().filter(filter).collect(Collectors.toSet()));

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
