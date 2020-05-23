package com.exemple.service.schema.validation;

import org.everit.json.schema.Schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public interface SchemaValidation {

    void validate(String app, String version, String resource, String profile, JsonNode form, JsonNode old);

    void validate(Schema schema, JsonNode target);

    void validatePatch(ArrayNode patch);

}
