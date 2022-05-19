package com.exemple.service.schema.filter.impl;

import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.SchemaValidator;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.JsonPatch;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaFilterImpl implements SchemaFilter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaBuilder schemaBuilder;

    @Override
    public JsonNode filter(String app, String version, String resource, String profile, JsonNode source) {

        JsonNode filterProperties = source.deepCopy();

        Schema schema = schemaBuilder.buildUpdateSchema(app, version, resource, profile);

        SchemaValidator.performValidation(schema, ReadWriteContext.READ, source, (ValidationException e) ->

        e.getCauses().stream()
                .filter((ValidationExceptionCause cause) -> isAdditionalProperties(cause) || isWriteOnly(cause))
                .map((ValidationExceptionCause cause) -> {
                    ObjectNode patch = MAPPER.createObjectNode();
                    patch.put("op", "remove");
                    patch.put("path", cause.getPath());
                    return patch;
                })
                .map((JsonNode patch) -> {
                    ArrayNode patchs = MAPPER.createArrayNode();
                    patchs.add(patch);
                    return patchs;
                })
                .forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, filterProperties)));
        return filterProperties;
    }

    @Override
    public JsonNode filterAllProperties(String app, String version, String resource, String profile, JsonNode source) {

        JsonNode allProperties = source.deepCopy();
        Schema schema = schemaBuilder.buildUpdateSchema(app, version, resource, profile);

        SchemaValidator.performValidation(schema, ReadWriteContext.READ, source, (ValidationException e) ->

        e.getCauses().stream()
                .filter(SchemaFilterImpl::isAdditionalProperties)
                .map((ValidationExceptionCause cause) -> {
                    ObjectNode patch = MAPPER.createObjectNode();
                    patch.put("op", "remove");
                    patch.put("path", cause.getPath());
                    return patch;
                })
                .map((JsonNode patch) -> {
                    ArrayNode patchs = MAPPER.createArrayNode();
                    patchs.add(patch);
                    return patchs;
                })
                .forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, allProperties)));
        return allProperties;
    }

    @Override
    public JsonNode filterAllAdditionalProperties(String app, String version, String resource, String profile, JsonNode source) {

        JsonNode onlyAdditionalProperties = MAPPER.createObjectNode();
        Schema schema = schemaBuilder.buildUpdateSchema(app, version, resource, profile);

        SchemaValidator.performValidation(schema, ReadWriteContext.READ, source, (ValidationException e) ->

        e.getCauses().stream()
                .filter(SchemaFilterImpl::isAdditionalProperties)
                .filter((ValidationExceptionCause cause) -> JsonPointer.empty().equals(cause.getPointer().head()))
                .map((ValidationExceptionCause cause) -> {
                    ObjectNode patch = MAPPER.createObjectNode();
                    patch.put("op", "add");
                    patch.put("path", cause.getPath());
                    patch.set("value", cause.getValue());
                    return patch;
                })
                .map((JsonNode patch) -> {
                    ArrayNode patchs = MAPPER.createArrayNode();
                    patchs.add(patch);
                    return patchs;
                })
                .forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, onlyAdditionalProperties)));
        return onlyAdditionalProperties;
    }

    private static boolean isAdditionalProperties(ValidationExceptionCause cause) {
        return "additionalProperties".equals(cause.getCode());
    }

    private static boolean isWriteOnly(ValidationExceptionCause cause) {
        return "writeOnly".equals(cause.getCode());
    }

}
