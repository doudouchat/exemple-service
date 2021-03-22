package com.exemple.service.schema.merge;

import com.fasterxml.jackson.databind.JsonNode;

public interface SchemaMerge {

    void mergeMissingFieldFromOriginal(String app, String version, String resource, String profile, JsonNode source, JsonNode orignalSource);

}
