package com.exemple.service.schema.validation;

import static com.flipkart.zjsonpatch.CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.SchemaValidator;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.flipkart.zjsonpatch.JsonPatch;
import com.networknt.schema.Schema;

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
        var readonOnlyAndAdditionalPropertiesExceptions = findReadOnlyAndAdditionalPropertiesExceptionsWithRemoveOperation(schema, patch,
                oldFilterBySchema);

        JsonNode form = JsonPatch.apply(patch, oldFilterBySchema,
                EnumSet.of(ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));
        var allExceptions = ListUtils.union(readonOnlyAndAdditionalPropertiesExceptions, SchemaValidator.findValidationExceptionCauses(schema, form));
        if (!allExceptions.isEmpty()) {
            var validationException = new ValidationException(allExceptions);

            Set<ValidationExceptionCause> previousExceptions = findDistinctExceptions(schema, oldFilterBySchema);

            Predicate<ValidationExceptionCause> newExceptionFilter = (ValidationExceptionCause cause) -> isExceptionNotAlreadyExists(cause,
                    previousExceptions);

            throwExceptionIfCausesNotEmpty(validationException, newExceptionFilter);
        }

    }

    public void validate(Schema schema, JsonNode target) {

        SchemaValidator.performValidation(schema, target);

    }

    private static Set<ValidationExceptionCause> findDistinctExceptions(Schema schema, JsonNode target) {

        Set<ValidationExceptionCause> exceptions = new HashSet<>();
        SchemaValidator.performValidation(schema, target, (ValidationException e) -> e.getCauses().forEach(exceptions::add));
        return exceptions;

    }

    private static void throwExceptionIfCausesNotEmpty(ValidationException e, Predicate<ValidationExceptionCause> filter) {

        var validationException = new ValidationException(e.getCauses().stream().filter(filter).toList());

        if (!validationException.getCauses().isEmpty()) {
            throw validationException;

        }

    }

    private static boolean isExceptionNotAlreadyExists(ValidationExceptionCause cause, Set<ValidationExceptionCause> previousExceptions) {

        return !previousExceptions.contains(cause);
    }

    private static List<ValidationExceptionCause> findReadOnlyAndAdditionalPropertiesExceptionsWithRemoveOperation(Schema schema,
            ArrayNode patch, JsonNode source) {

        var patchWithOnlyRemoveOperation = patch.deepCopy();
        patchWithOnlyRemoveOperation.valueStream()
                .filter((JsonNode p) -> "remove".equals(p.get("op").textValue()))
                .map(ObjectNode.class::cast)
                .forEach((ObjectNode p) -> {
                    p.replace("op", new TextNode("replace"));
                    p.replace("value", NullNode.instance);
                });
        var form = JsonPatch.apply(patchWithOnlyRemoveOperation, source,
                EnumSet.of(ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));

        return SchemaValidator.findValidationExceptionCauses(schema, form).stream()
                .filter(SchemaFilter::isAdditionalOrReadOnlyProperties)
                .toList();
    }

}
