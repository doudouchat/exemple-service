package com.exemple.service.schema.description;

import com.fasterxml.jackson.databind.JsonNode;

public interface SchemaDescription {

    JsonNode get(String app, String version, String resource, String profile);

    JsonNode getPatch();
}
