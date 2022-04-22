package com.exemple.service.schema.description.impl;

import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SchemaDescriptionImpl implements SchemaDescription {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonNode patchSchema;

    private final JsonNode defaultSchema;

    private final SchemaResource schemaResource;

    public SchemaDescriptionImpl(SchemaResource schemaResource) throws IOException {

        this.schemaResource = schemaResource;
        this.patchSchema = MAPPER.readTree(new ClassPathResource("json-patch.json").getInputStream());
        this.defaultSchema = MAPPER.readTree(new ClassPathResource("default-schema.json").getInputStream());

    }

    @Override
    public JsonNode get(String app, String version, String resource, String profile) {

        return schemaResource.get(app, version, resource, profile).map(SchemaEntity::getContent).orElse(defaultSchema);
    }

    @Override
    public JsonNode getPatch() {

        return patchSchema;
    }

}
