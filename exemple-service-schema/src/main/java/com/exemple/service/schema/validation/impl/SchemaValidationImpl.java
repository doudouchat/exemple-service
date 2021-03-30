package com.exemple.service.schema.validation.impl;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionBuilder;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.CompatibilityFlags;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.common.base.Predicates;
import com.google.common.collect.Streams;

@Component
public class SchemaValidationImpl implements SchemaValidation {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    private final Schema patchSchema;

    public SchemaValidationImpl(SchemaResource schemaResource, Schema patchSchema) {

        this.schemaResource = schemaResource;
        this.patchSchema = patchSchema;
    }

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form) {

        SchemaEntity schemaEntity = schemaResource.get(app, version, resource, profile);
        Schema schema = buildSchema(schemaEntity.getContent(), Collections.emptySet());
        try {
            performValidation(schema, form);
        } catch (org.everit.json.schema.ValidationException e) {

            ValidationException validationException = new ValidationException(e);
            ValidationExceptionBuilder.buildException(e).stream().forEach(validationException::add);

            throw validationException;

        }

    }

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form, JsonNode old) {

        SchemaEntity schemaEntity = schemaResource.get(app, version, resource, profile);
        Schema schema = buildSchema(schemaEntity.getContent(), schemaEntity.getPatchs());

        validateDiff(schema, form, old, Predicates.alwaysTrue());

        checkRemove(schema, form, old);

    }

    private static void validateDiff(Schema schema, JsonNode form, JsonNode old, Predicate<ValidationExceptionModel> filter) {

        try {
            performValidation(schema, form);
        } catch (org.everit.json.schema.ValidationException e) {

            Set<JsonPointer> paths = diff(old, form);
            Set<ValidationExceptionModel> previousExceptions = findDistinctExceptions(schema, old);

            Predicate<ValidationExceptionModel> newExceptionFilter = (ValidationExceptionModel cause) -> isNewException(cause, paths)
                    || isExceptionNotAlreadyExists(cause, previousExceptions);

            ValidationException validationException = new ValidationException();
            ValidationExceptionBuilder.buildException(e).stream().filter(filter).filter(newExceptionFilter).forEach(validationException::add);

            if (!validationException.getAllExceptions().isEmpty()) {
                throw validationException;

            }
        }

    }

    private static void checkRemove(Schema schema, JsonNode form, JsonNode old) {

        Set<JsonNode> paths = diffTestOperation(old, form);
        List<JsonNode> patchs = paths.stream().map((JsonNode element) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> values = MAPPER.convertValue(element, Map.class);
            values.put("op", "add");
            return MAPPER.convertValue(values, JsonNode.class);
        }).collect(Collectors.toList());
        JsonNode source = JsonPatch.apply(MAPPER.createArrayNode().addAll(patchs), form, EnumSet.of(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
        validateDiff(schema, source, form,
                (ValidationExceptionModel cause) -> ArrayUtils.contains(new String[] { "readOnly", "additionalProperties" }, cause.getCode()));

    }

    @Override
    public void validate(Schema schema, JsonNode target) {

        try {
            performValidation(schema, target);
        } catch (org.everit.json.schema.ValidationException e) {

            ValidationException validationException = new ValidationException(e);
            ValidationExceptionBuilder.buildException(e).forEach(validationException::add);

            throw validationException;
        }

    }

    @Override
    public void validatePatch(ArrayNode patch) {

        JSONArray jsonArray = new JSONArray();
        Streams.stream(patch.elements()).forEach(n -> jsonArray.put(MAPPER.convertValue(n, Map.class)));

        try {
            patchSchema.validate(jsonArray);

        } catch (org.everit.json.schema.ValidationException e) {

            ValidationException validationException = new ValidationException(e);
            ValidationExceptionBuilder.buildException(e).forEach(validationException::add);

            throw validationException;
        }

    }

    private static void performValidation(Schema schema, JsonNode form) {

        Validator validator = Validator.builder().readWriteContext(ReadWriteContext.WRITE).build();
        if (form.isArray()) {
            @SuppressWarnings("unchecked")
            List<Object> formMap = MAPPER.convertValue(form, List.class);
            validator.performValidation(schema, new JSONArray(formMap));
        } else {
            @SuppressWarnings("unchecked")
            Map<String, Object> formMap = MAPPER.convertValue(form, Map.class);
            validator.performValidation(schema, new JSONObject(formMap));
        }

    }

    private static Set<ValidationExceptionModel> findDistinctExceptions(Schema schema, JsonNode target) {

        Set<ValidationExceptionModel> exceptions = new HashSet<>();
        try {
            performValidation(schema, target);
        } catch (org.everit.json.schema.ValidationException e) {

            ValidationExceptionBuilder.buildException(e).forEach(exceptions::add);

        }
        return exceptions;

    }

    private static Schema buildSchema(JsonNode schema, Set<JsonNode> patchs) {

        ArrayNode patch = MAPPER.createArrayNode().addAll(patchs);
        JSONObject rawSchema = new JSONObject(new JSONTokener(JsonPatch.apply(patch, schema).toString()));

        SchemaLoader schemaLoader = SchemaLoader.builder().draftV7Support().schemaJson(rawSchema).build();
        return schemaLoader.load().build();

    }

    private static boolean isNewException(ValidationExceptionModel cause, Set<JsonPointer> paths) {

        if (!cause.getPointer().head().equals(JsonPointer.empty())) {
            return paths.contains(cause.getPointer().head());
        }

        return paths.contains(cause.getPointer());
    }

    private static boolean isExceptionNotAlreadyExists(ValidationExceptionModel cause, Set<ValidationExceptionModel> previousExceptions) {

        return !previousExceptions.contains(cause);
    }

    private static Set<JsonPointer> diff(JsonNode source, final JsonNode target) {

        ArrayNode patch = (ArrayNode) JsonDiff.asJson(source, target);
        return Streams.stream(patch.elements()).flatMap((JsonNode element) -> {
            JsonPointer path = JsonPointer.compile(element.get("path").textValue());
            if (!path.head().equals(JsonPointer.empty())) {
                return Stream.of(path.head(), path);
            }
            return Stream.of(path);
        }).collect(Collectors.toSet());
    }

    private static Set<JsonNode> diffTestOperation(JsonNode source, final JsonNode target) {

        ArrayNode patch = (ArrayNode) JsonDiff.asJson(source, target, EnumSet.of(DiffFlags.EMIT_TEST_OPERATIONS));
        return Streams.stream(patch.elements()).filter((JsonNode element) -> "test".equals(element.get("op").textValue()))
                .collect(Collectors.toSet());
    }

}
