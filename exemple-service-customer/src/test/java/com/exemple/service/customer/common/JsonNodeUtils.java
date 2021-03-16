package com.exemple.service.customer.common;

import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonNodeUtils {

    private JsonNodeUtils() {

    }

    public static JsonNode set(JsonNode node, String field, JsonNode data) {

        ObjectNode clone = node.deepCopy();

        clone.set(field, data);

        return clone;
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode create(Supplier<?> source) {

        return MAPPER.convertValue(source.get(), JsonNode.class);

    }

}
