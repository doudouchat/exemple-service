package com.exemple.service.schema.validation.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
import com.exemple.service.schema.common.FilterBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionBuilder;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.validation.SchemaValidation;
import com.exemple.service.schema.validation.custom.CustomDateTimeFormatValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaValidationImpl implements SchemaValidation {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form) {

        SchemaEntity schemaEntity = schemaResource.get(app, version, resource, profile);
        Schema schema = buildSchema(schemaEntity.getContent(), Collections.emptySet());
        performValidation(schema, form);

    }

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form, JsonNode old) {

        SchemaEntity schemaEntity = schemaResource.get(app, version, resource, profile);
        Schema schema = buildSchema(schemaEntity.getContent(), schemaEntity.getPatchs());

        try {
            performValidation(schema, form);
        } catch (ValidationException e) {

            Predicate<ValidationExceptionCause> newExceptionFilter = (
                    ValidationExceptionCause cause) -> !isReadOnlyAndSourceIsNotModified(cause, old);

            throwExceptionIfCausesNotEmpty(e, newExceptionFilter);
        }

    }

    @Override
    public void validate(String app, String version, String profile, String resource, ArrayNode patch, JsonNode old) {

        SchemaEntity schemaEntity = schemaResource.get(app, version, resource, profile);
        Schema schema = buildSchema(schemaEntity.getContent(), schemaEntity.getPatchs());

        JsonNode oldFilterBySchema = FilterBuilder.filter(old, schemaEntity.getFields().toArray(new String[0]));

        JsonNode form = JsonPatch.apply(patch, oldFilterBySchema);

        try {
            performValidation(schema, form);
        } catch (ValidationException e) {

            Set<ValidationExceptionCause> previousExceptions = findDistinctExceptions(schema, oldFilterBySchema);

            Predicate<ValidationExceptionCause> newExceptionFilter = (ValidationExceptionCause cause) -> isExceptionNotAlreadyExists(cause,
                    previousExceptions);

            throwExceptionIfCausesNotEmpty(e, newExceptionFilter);

        }

    }

    @Override
    public void validate(Schema schema, JsonNode target) {

        performValidation(schema, target);

    }

    private static void performValidation(Schema schema, JsonNode form) {

        Validator validator = Validator.builder().readWriteContext(ReadWriteContext.WRITE).build();

        try {

            if (form.isArray()) {
                @SuppressWarnings("unchecked")
                List<Object> formMap = MAPPER.convertValue(form, List.class);
                validator.performValidation(schema, new JSONArray(formMap));
            } else {
                @SuppressWarnings("unchecked")
                Map<String, Object> formMap = MAPPER.convertValue(form, Map.class);
                validator.performValidation(schema, new JSONObject(formMap));
            }

        } catch (org.everit.json.schema.ValidationException e) {

            throw new ValidationException(ValidationExceptionBuilder.buildException(e, form));
        }

    }

    private static Set<ValidationExceptionCause> findDistinctExceptions(Schema schema, JsonNode target) {

        Set<ValidationExceptionCause> exceptions = new HashSet<>();
        try {
            performValidation(schema, target);
        } catch (ValidationException e) {

            e.getCauses().forEach(exceptions::add);

        }
        return exceptions;

    }

    private static void throwExceptionIfCausesNotEmpty(ValidationException e, Predicate<ValidationExceptionCause> filter) {

        ValidationException validationException = new ValidationException(
                e.getCauses().stream().filter(filter).collect(Collectors.toSet()));

        if (!validationException.getCauses().isEmpty()) {
            throw validationException;

        }

    }

    private static Schema buildSchema(JsonNode schema, Set<JsonNode> patchs) {

        ArrayNode patch = MAPPER.createArrayNode().addAll(patchs);
        JSONObject rawSchema = new JSONObject(new JSONTokener(JsonPatch.apply(patch, schema).toString()));

        SchemaLoader schemaLoader = SchemaLoader.builder().draftV7Support().schemaJson(rawSchema)
                .addFormatValidator(new CustomDateTimeFormatValidator()).enableOverrideOfBuiltInFormatValidators().build();
        return schemaLoader.load().build();

    }

    private static boolean isExceptionNotAlreadyExists(ValidationExceptionCause cause, Set<ValidationExceptionCause> previousExceptions) {

        return !previousExceptions.contains(cause);
    }

    private static boolean isReadOnlyAndSourceIsNotModified(ValidationExceptionCause cause, JsonNode previousSource) {

        return "readOnly".equals(cause.getCode()) && previousSource.at(cause.getPath()).equals(cause.getValue());
    }

}
