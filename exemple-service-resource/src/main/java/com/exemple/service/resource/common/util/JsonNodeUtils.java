package com.exemple.service.resource.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonNodeUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode init() {

        return MAPPER.createObjectNode();
    }

    public static void set(JsonNode node, Object data, String field) {

        ((ObjectNode) node).set(field, create(data));
    }

    public static JsonNode create(Object data) {

        return MAPPER.convertValue(data, JsonNode.class);

    }

}
