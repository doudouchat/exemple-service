package com.exemple.service.schema.description.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SchemaDescriptionImpl implements SchemaDescription {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JsonNode patchSchema;

    private final SchemaResource schemaResource;

    public SchemaDescriptionImpl(SchemaResource schemaResource) throws IOException {

        this.schemaResource = schemaResource;
        this.patchSchema = MAPPER.readTree(new ClassPathResource("json-patch.json").getInputStream());

    }

    @Override
    public JsonNode get(String app, String version, String resource, String profile) {

        JSONObject rawSchema = new JSONObject(
                new JSONTokener(new ByteArrayInputStream(schemaResource.get(app, version, resource, profile).getContent())));
        return MAPPER.convertValue(rawSchema.toMap(), JsonNode.class);
    }

    @Override
    public JsonNode getPatch() {

        return patchSchema;
    }

}
