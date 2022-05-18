package com.exemple.service.schema.validation.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionBuilder;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
import com.exemple.service.schema.validation.custom.CustomDateTimeFormatValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.CompatibilityFlags;
import com.flipkart.zjsonpatch.JsonPatch;

@Component
public class SchemaValidationImpl implements SchemaValidation {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    private final Schema defaultSchema;

    private final SchemaFilter schemaFilter;

    public SchemaValidationImpl(SchemaResource schemaResource, SchemaFilter schemaFilter) throws IOException {
        this.schemaResource = schemaResource;
        this.schemaFilter = schemaFilter;

        JSONObject schemaJson = new JSONObject(new JSONTokener(new ClassPathResource("default-schema.json").getInputStream()));
        defaultSchema = buildSchema(schemaJson);

    }

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form) {

        Schema schema = schemaResource.get(app, version, resource, profile)
                .map(SchemaEntity::getContent)
                .map((JsonNode schemaContent) -> buildSchema(schemaContent, Collections.emptySet()))
                .orElse(defaultSchema);
        performValidation(schema, form);

    }

    @Override
    public void validate(String app, String version, String profile, String resource, JsonNode form, JsonNode old) {

        Schema schema = schemaResource.get(app, version, resource, profile)
                .filter((SchemaEntity schemaEntity) -> schemaEntity.getContent() != null)
                .map((SchemaEntity schemaEntity) -> buildSchema(schemaEntity.getContent(), schemaEntity.getPatchs()))
                .orElse(defaultSchema);

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

        Optional<SchemaEntity> schemaEntity = schemaResource.get(app, version, resource, profile);

        Schema schema = schemaEntity
                .filter((SchemaEntity entity) -> entity.getContent() != null)
                .map((SchemaEntity entity) -> buildSchema(entity.getContent(), entity.getPatchs()))
                .orElse(defaultSchema);

        JsonNode oldFilterBySchema = this.schemaFilter.filterAllProperties(app, version, resource, profile, old);

        JsonNode form = JsonPatch.apply(patch, oldFilterBySchema,
                EnumSet.of(CompatibilityFlags.FORBID_REMOVE_MISSING_OBJECT, CompatibilityFlags.ALLOW_MISSING_TARGET_OBJECT_ON_REPLACE));

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
                validator.performValidation(schema, new JSONArray(form.toString()));
            } else {
                validator.performValidation(schema, new JSONObject(form.toString()));
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
        return buildSchema(rawSchema);

    }

    private static Schema buildSchema(JSONObject rawSchema) {

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
