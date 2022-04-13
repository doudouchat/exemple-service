package com.exemple.service.schema.validation;

import org.everit.json.schema.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface SchemaValidation {

    void validate(String app, String version, String profile, String resource, JsonNode form);

    void validate(String app, String version, String profile, String resource, JsonNode form, JsonNode old);

    void validate(String app, String version, String profile, String resource, ArrayNode patch, JsonNode old);

    void validate(Schema schema, JsonNode target);

}
