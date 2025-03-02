package com.exemple.service.schema.filter;

import java.util.List;

import org.springframework.stereotype.Component;

import com.exemple.service.schema.common.SchemaBuilder;
import com.exemple.service.schema.common.SchemaValidator;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaFilter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaBuilder schemaBuilder;

    public JsonNode filter(String resource, String version, String profile, JsonNode source) {

        var schema = schemaBuilder.buildFilterSchema(resource, version, profile);
        List<ArrayNode> exceptions = SchemaValidator.performValidation(schema, source,
                (ValidationException e) -> e.getCauses().stream()
                        .filter((ValidationExceptionCause cause) -> isAdditionalProperties(cause) || isWriteOnly(cause))
                        .map((ValidationExceptionCause cause) -> {
                            var patch = MAPPER.createObjectNode();
                            patch.put("op", "remove");
                            patch.put("path", cause.getPath());
                            return patch;
                        }).map((JsonNode patch) -> {
                            var patchs = MAPPER.createArrayNode();
                            patchs.add(patch);
                            return patchs;
                        }).toList());
        JsonNode filterProperties = source.deepCopy();
        exceptions.forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, filterProperties));
        return filterProperties;
    }

    public JsonNode filterAllProperties(String resource, String version, String profile, JsonNode source) {

        var schema = schemaBuilder.buildFilterSchema(resource, version, profile);

        List<ArrayNode> exceptions = SchemaValidator.performValidation(schema, source,
                (ValidationException e) -> e.getCauses().stream()
                        .filter(SchemaFilter::isAdditionalProperties)
                        .map((ValidationExceptionCause cause) -> {
                            var patch = MAPPER.createObjectNode();
                            patch.put("op", "remove");
                            patch.put("path", cause.getPath());
                            return patch;
                        }).map((JsonNode patch) -> {
                            var patchs = MAPPER.createArrayNode();
                            patchs.add(patch);
                            return patchs;
                        }).toList());
        JsonNode allProperties = source.deepCopy();
        exceptions.forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, allProperties));
        return allProperties;
    }

    public JsonNode filterAllAdditionalAndReadOnlyProperties(String resource, String version, String profile, JsonNode source) {

        var schema = schemaBuilder.buildFilterSchema(resource, version, profile);

        List<ArrayNode> exceptions = SchemaValidator.performValidation(schema, source,
                (ValidationException e) -> e.getCauses().stream()
                        .filter(SchemaFilter::isAdditionalOrReadOnlyProperties)
                        .filter((ValidationExceptionCause cause) -> JsonPointer.empty().equals(cause.getPointer().head()))
                        .map((ValidationExceptionCause cause) -> {
                            var patch = MAPPER.createObjectNode();
                            patch.put("op", "add");
                            patch.put("path", cause.getPath());
                            patch.set("value", cause.getValue());
                            return patch;
                        })
                        .map((JsonNode patch) -> {
                            var patchs = MAPPER.createArrayNode();
                            patchs.add(patch);
                            return patchs;
                        }).toList());
        JsonNode onlyAdditionalProperties = MAPPER.createObjectNode();
        exceptions.forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, onlyAdditionalProperties));
        return onlyAdditionalProperties;
    }

    private static boolean isAdditionalProperties(ValidationExceptionCause cause) {
        return "additionalProperties".equals(cause.getCode());
    }

    private static boolean isAdditionalOrReadOnlyProperties(ValidationExceptionCause cause) {
        return List.of("additionalProperties", "readOnly").contains(cause.getCode());
    }

    private static boolean isWriteOnly(ValidationExceptionCause cause) {
        return "writeOnly".equals(cause.getCode());
    }

}
