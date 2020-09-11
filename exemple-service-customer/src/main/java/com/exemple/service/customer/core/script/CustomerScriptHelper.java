package com.exemple.service.customer.core.script;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class CustomerScriptHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private CustomerScriptHelper() {

    }

    public static JsonNode execute(JsonNode source, JsonNode previousSource, CustomiseResource customiseResource) {
        if (previousSource != null) {
            return execute(source, previousSource, customiseResource::update);
        }
        return execute(source, customiseResource::create);
    }

    private static JsonNode execute(JsonNode source, Function<Map<String, Object>, Map<String, Object>> service) {

        if (source != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
            return MAPPER.convertValue(service.apply(sourceMap), JsonNode.class);
        } else {
            return MAPPER.createObjectNode();
        }

    }

    private static JsonNode execute(JsonNode source, JsonNode previousSource,
            BiFunction<Map<String, Object>, Map<String, Object>, Map<String, Object>> service) {

        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> previousMap = MAPPER.convertValue(previousSource, Map.class);
        return MAPPER.convertValue(service.apply(sourceMap, previousMap), JsonNode.class);

    }

}
