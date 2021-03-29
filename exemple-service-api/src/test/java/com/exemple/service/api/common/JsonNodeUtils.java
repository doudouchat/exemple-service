package com.exemple.service.api.common;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class JsonNodeUtils {

    private JsonNodeUtils() {

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static JsonNode set(JsonNode node, String field, JsonNode data) {

        ObjectNode clone = node.deepCopy();

        clone.set(field, data);

        return clone;
    }

    public static JsonNode remove(JsonNode node, String field) {

        ObjectNode clone = node.deepCopy();

        clone.remove(field);

        return clone;
    }

    public static String toString(List<Map<String, Object>> patchs) {

        try {
            return MAPPER.writeValueAsString(patchs);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static JsonNode create(String resourceName) {

        try {
            return MAPPER.readTree(ResourceUtils.getFile(resourceName));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    public static JsonNode create(Supplier<?> source) {

        return MAPPER.convertValue(source.get(), JsonNode.class);

    }

    public static ArrayNode toArrayNode(List<JsonNode> nodes) {

        return MAPPER.createArrayNode().addAll(nodes);

    }

}
