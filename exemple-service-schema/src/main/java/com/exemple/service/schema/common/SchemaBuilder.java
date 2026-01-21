package com.exemple.service.schema.common;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.flipkart.zjsonpatch.Jackson3JsonPatch;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.SchemaRegistryConfig;
import com.networknt.schema.SpecificationVersion;
import com.networknt.schema.path.PathType;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class SchemaBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    private final Schema defaultSchema;

    private final Schema patchSchema;

    public SchemaBuilder(SchemaResource schemaResource) throws IOException {

        this.schemaResource = schemaResource;

        var schemaJson = MAPPER.readTree(new ClassPathResource("default-schema.json").getInputStream());
        defaultSchema = SchemaBuilder.buildSchema(schemaJson);

        patchSchema = SchemaBuilder.buildSchema(MAPPER.readTree(new ClassPathResource("json-patch.json").getInputStream()));

    }

    public Schema buildPatchSchema() {

        return patchSchema;
    }

    public Schema buildCreationValidationSchema(String resource, String version, String profile) {

        return schemaResource.get(resource, version, profile)
                .map(SchemaEntity::getContent)
                .map((JsonNode schemaContent) -> SchemaBuilder.buildSchema(schemaContent, Collections.emptySet()))
                .orElse(defaultSchema);
    }

    public Schema buildUpdateValidationSchema(String resource, String version, String profile) {

        return schemaResource.get(resource, version, profile)
                .filter((SchemaEntity entity) -> entity.getContent() != null)
                .map((SchemaEntity entity) -> SchemaBuilder.buildSchema(entity.getContent(), entity.getPatchs()))
                .orElse(defaultSchema);
    }

    private static Schema buildSchema(JsonNode schema, Set<JsonNode> patchs) {

        var patch = MAPPER.createArrayNode().addAll(patchs);
        var rawSchema = Jackson3JsonPatch.apply(patch, schema);
        return buildSchema(rawSchema);

    }

    private static Schema buildSchema(JsonNode rawSchema) {

        var factory = SchemaRegistry.builder()
                .defaultDialectId(SpecificationVersion.DRAFT_2019_09.getDialectId())
                .schemaRegistryConfig(SchemaRegistryConfig.builder()
                        .pathType(PathType.JSON_POINTER)
                        .build())
                .build();
        return factory.getSchema(rawSchema);

    }

}
