package com.exemple.service.schema.common;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.validation.custom.CustomDateTimeFormatValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

@Component
public class SchemaBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    private final Schema defaultSchema;

    public SchemaBuilder(SchemaResource schemaResource) throws IOException {

        this.schemaResource = schemaResource;

        JSONObject schemaJson = new JSONObject(new JSONTokener(new ClassPathResource("default-schema.json").getInputStream()));
        defaultSchema = SchemaBuilder.buildSchema(schemaJson);
    }

    public static Schema build(InputStream source) {

        return buildSchema(new JSONObject(new JSONTokener(source)));
    }

    public Schema buildCreationSchema(String app, String version, String resource, String profile) {

        return schemaResource.get(app, version, resource, profile)
                .map(SchemaEntity::getContent)
                .map((JsonNode schemaContent) -> SchemaBuilder.buildSchema(schemaContent, Collections.emptySet()))
                .orElse(defaultSchema);
    }

    public Schema buildUpdateSchema(String app, String version, String resource, String profile) {

        return schemaResource.get(app, version, resource, profile)
                .filter((SchemaEntity entity) -> entity.getContent() != null)
                .map((SchemaEntity entity) -> SchemaBuilder.buildSchema(entity.getContent(), entity.getPatchs()))
                .orElse(defaultSchema);
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

}
