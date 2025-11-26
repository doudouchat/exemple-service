package com.exemple.service.schema.validation;

import static com.flipkart.zjsonpatch.CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.SchemaValidator;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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

    public void validate(String resource, String version, String profile, ArrayNode patch, JsonNode old) {

        var oldFilterBySchema = this.schemaFilter.filterAllProperties(resource, version, profile, old);
        var schema = schemaBuilder.buildUpdateValidationSchema(resource, version, profile);
        var readonOnlyExceptions = findReadOnlyAndAdditionalPropertiesExceptionsWithRemoveOperation(schema, patch);

        JsonNode form = JsonPatch.apply(patch, oldFilterBySchema,
                EnumSet.of(ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));
        var allExceptions = SetUtils.union(readonOnlyExceptions, SchemaValidator.findValidationExceptionCauses(schema, form)).toSet();
        if (!allExceptions.isEmpty()) {
            var validationException = new ValidationException(allExceptions);

            Set<ValidationExceptionCause> previousExceptions = findDistinctExceptions(schema, oldFilterBySchema);

            Predicate<ValidationExceptionCause> newExceptionFilter = (ValidationExceptionCause cause) -> isExceptionNotAlreadyExists(cause,
                    previousExceptions);

            throwExceptionIfCausesNotEmpty(validationException, newExceptionFilter);
        }

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

    private static Set<ValidationExceptionCause> findReadOnlyAndAdditionalPropertiesExceptionsWithRemoveOperation(JsonSchema schema,
            ArrayNode patch) {

        return patch.valueStream()
                .filter((JsonNode p) -> "remove".equals(p.get("op").textValue()))
                .map((JsonNode p) -> StringUtils.substringBefore(p.get("path").textValue().substring(1), "/"))
                .map((String field) -> JsonPointer.compile("/" + field))
                .mapMulti((JsonPointer path, Consumer<ValidationExceptionCause> check) -> {
                    JsonNode property = schema.getSchemaNode().at("/properties" + path);
                    if (property.path("readOnly").asBoolean(false)) {
                        check.accept(ValidationExceptionCause.builder()
                                .code("readOnly")
                                .pointer(path)
                                .build());
                    }

                    if (property.isMissingNode()) {
                        check.accept(ValidationExceptionCause.builder()
                                .code("additionalProperties")
                                .pointer(path)
                                .build());
                    }
                })
                .collect(Collectors.toSet());
    }

}
