package com.exemple.service.customer.common;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TransformUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TransformUtils() {

    }

    public static JsonNode apply(JsonNode source, Function<Map<String, Object>, Map<String, Object>> service) {

        if (source != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
            return MAPPER.convertValue(service.apply(sourceMap), JsonNode.class);
        } else {
            return MAPPER.createObjectNode();
        }

    }

    @SuppressWarnings("unchecked")
    public static void accept(JsonNode source, Consumer<Map<String, Object>> service) {

        Map<String, Object> formMap = MAPPER.convertValue(source, Map.class);
        service.accept(formMap);
    }

    @SuppressWarnings("unchecked")
    public static void accept(JsonNode source1, JsonNode source2, BiConsumer<Map<String, Object>, Map<String, Object>> service) {

        Map<String, Object> formMap = MAPPER.convertValue(source1, Map.class);
        Map<String, Object> oldMap = MAPPER.convertValue(source2, Map.class);
        service.accept(formMap, oldMap);
    }

}
