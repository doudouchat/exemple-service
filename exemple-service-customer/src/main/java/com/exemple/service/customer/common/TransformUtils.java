package com.exemple.service.customer.common;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class TransformUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private TransformUtils() {

    }

    public static JsonNode transform(JsonNode source, Function<Map<String, Object>, Map<String, Object>> service) {

        if (source != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
            return JsonNodeUtils.create(service.apply(sourceMap));
        } else {
            return JsonNodeUtils.init();
        }
    }

    @SuppressWarnings("unchecked")
    public static void transform(JsonNode source1, JsonNode source2, BiConsumer<Map<String, Object>, Map<String, Object>> service) {

        Map<String, Object> formMap = MAPPER.convertValue(source1, Map.class);
        Map<String, Object> oldMap = null;
        if (source2 != null) {
            oldMap = MAPPER.convertValue(source2, Map.class);
        }
        service.accept(formMap, oldMap);
    }

}
