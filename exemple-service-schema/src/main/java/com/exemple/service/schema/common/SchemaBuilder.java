package com.exemple.service.schema.common;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonPatch;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersionDetector;

@Component
public class SchemaBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    private final JsonSchema defaultSchema;

    private final JsonSchema patchSchema;

    public SchemaBuilder(SchemaResource schemaResource) throws IOException {

        this.schemaResource = schemaResource;

        var schemaJson = MAPPER.readTree(new ClassPathResource("default-schema.json").getInputStream());
        defaultSchema = SchemaBuilder.buildSchema(schemaJson, false);

        patchSchema = SchemaBuilder.buildSchema(MAPPER.readTree(new ClassPathResource("json-patch.json").getInputStream()), false);

    }

    public JsonSchema buildPatchSchema() {

        return patchSchema;
    }

    public JsonSchema buildCreationValidationSchema(String resource, String version, String profile) {

        return buildCreationSchema(resource, version, profile, false);
    }

    public JsonSchema buildUpdateValidationSchema(String resource, String version, String profile) {

        return buildUpdateSchema(resource, version, profile, false);
    }

    public JsonSchema buildFilterSchema(String resource, String version, String profile) {

        return buildUpdateSchema(resource, version, profile, true);
    }

    private JsonSchema buildCreationSchema(String resource, String version, String profile, boolean checkWriteOnly) {

        return schemaResource.get(resource, version, profile)
                .map(SchemaEntity::getContent)
                .map((JsonNode schemaContent) -> SchemaBuilder.buildSchema(schemaContent, Collections.emptySet(), checkWriteOnly))
                .orElse(defaultSchema);
    }

    private JsonSchema buildUpdateSchema(String resource, String version, String profile, boolean checkWriteOnly) {

        return schemaResource.get(resource, version, profile)
                .filter((SchemaEntity entity) -> entity.getContent() != null)
                .map((SchemaEntity entity) -> SchemaBuilder.buildSchema(entity.getContent(), entity.getPatchs(), checkWriteOnly))
                .orElse(defaultSchema);
    }

    private static JsonSchema buildSchema(JsonNode schema, Set<JsonNode> patchs, boolean checkWriteOnly) {

        var patch = MAPPER.createArrayNode().addAll(patchs);
        var rawSchema = JsonPatch.apply(patch, schema);
        return buildSchema(rawSchema, checkWriteOnly);

    }

    private static JsonSchema buildSchema(JsonNode rawSchema, boolean checkWriteOnly) {

        var factory = JsonSchemaFactory.getInstance(SpecVersionDetector.detect(rawSchema));
        var config = SchemaValidatorsConfig.builder()
                .readOnly(true)
                .writeOnly(checkWriteOnly)
                .pathType(PathType.JSON_POINTER)
                .formatAssertionsEnabled(true)
                .build();
        return factory.getSchema(rawSchema, config);

    }

}
