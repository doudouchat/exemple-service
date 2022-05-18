package com.exemple.service.schema.filter.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.FilterBuilder;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionCause;
import com.exemple.service.schema.filter.SchemaFilter;
import com.exemple.service.schema.validation.SchemaValidation;
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

    private final SchemaResource schemaResource;

    @Lazy
    private final SchemaValidation schemaValidation;

    @Override
    public JsonNode filter(String app, String version, String resource, String profile, JsonNode source) {

        return schemaResource.get(app, version, resource, profile)
                .map((SchemaEntity schema) -> FilterBuilder.filter(source, schema.getFilters().toArray(new String[0])))
                .orElse(MAPPER.createObjectNode());
    }

    @Override
    public JsonNode filterAllProperties(String app, String version, String resource, String profile, JsonNode source) {

        return schemaResource.get(app, version, resource, profile)
                .map((SchemaEntity schema) -> FilterBuilder.filter(source, schema.getFields().toArray(new String[0])))
                .orElse(MAPPER.createObjectNode());
    }

    @Override
    public JsonNode filterAllAdditionalProperties(String app, String version, String resource, String profile, JsonNode source) {

        JsonNode onlyAdditionalProperties = MAPPER.createObjectNode();
        try {
            schemaValidation.validate(app, version, profile, resource, source, onlyAdditionalProperties);
        } catch (ValidationException e) {

            e.getCauses().stream()
                    .filter((ValidationExceptionCause cause) -> "additionalProperties".equals(cause.getCode()))
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
                    .forEach((JsonNode patch) -> JsonPatch.applyInPlace(patch, onlyAdditionalProperties));

        }

        return onlyAdditionalProperties;
    }

}
