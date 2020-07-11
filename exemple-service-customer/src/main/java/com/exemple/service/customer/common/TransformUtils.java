package com.exemple.service.customer.common;

import java.util.Map;
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

}
