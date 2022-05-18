package com.exemple.service.schema.common;

import java.io.InputStream;
import java.util.Set;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.exemple.service.schema.validation.custom.CustomDateTimeFormatValidator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Schema build(InputStream source) {

        return buildSchema(new JSONObject(new JSONTokener(source)));
    }

    public static Schema buildSchema(JsonNode schema, Set<JsonNode> patchs) {

        ArrayNode patch = MAPPER.createArrayNode().addAll(patchs);
        JSONObject rawSchema = new JSONObject(new JSONTokener(JsonPatch.apply(patch, schema).toString()));
        return buildSchema(rawSchema);

    }

    public static Schema buildSchema(JSONObject rawSchema) {

        SchemaLoader schemaLoader = SchemaLoader.builder().draftV7Support().schemaJson(rawSchema)
                .addFormatValidator(new CustomDateTimeFormatValidator()).enableOverrideOfBuiltInFormatValidators().build();
        return schemaLoader.load().build();

    }

}
