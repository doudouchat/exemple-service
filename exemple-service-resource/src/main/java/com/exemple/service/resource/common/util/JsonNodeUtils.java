package com.exemple.service.resource.common.util;

import java.util.Arrays;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonNodeUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonNodeUtils() {

    }

    public static JsonNode clone(JsonNode resource, String... fields) {

        ObjectNode node = ((ObjectNode) resource).deepCopy();
        Arrays.stream(fields).forEach(node::remove);

        return node;
    }

    public static JsonNode init(String field) {

        ObjectNode node = (ObjectNode) init();
        node.set(field, null);

        return node;
    }

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
