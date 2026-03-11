package com.exemple.service.schema.description;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SchemaDescription {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonNode patchSchema;

    private final JsonNode defaultSchema;

    private final SchemaResource schemaResource;

    public SchemaDescription(SchemaResource schemaResource) throws IOException {

        this.schemaResource = schemaResource;
        this.patchSchema = MAPPER.readTree(new ClassPathResource("json-patch.json").getInputStream());
        this.defaultSchema = MAPPER.readTree(new ClassPathResource("default-schema.json").getInputStream());

    }

    public JsonNode get(String resource, String version, String profile) {

        return schemaResource.get(resource, version, profile).map(SchemaEntity::getContent).orElse(defaultSchema);
    }

    public JsonNode getPatch() {

        return patchSchema;
    }

}
