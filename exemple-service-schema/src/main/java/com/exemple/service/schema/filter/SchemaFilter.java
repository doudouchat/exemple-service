package com.exemple.service.schema.filter;

import com.fasterxml.jackson.databind.JsonNode;

public interface SchemaFilter {

    JsonNode filter(String app, String version, String resource, JsonNode form);

}
