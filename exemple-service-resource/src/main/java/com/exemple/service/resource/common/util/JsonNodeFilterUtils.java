package com.exemple.service.resource.common.util;

import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

public final class JsonNodeFilterUtils {

    private JsonNodeFilterUtils() {

    }

    public static void clean(JsonNode source) {

        filter(source, (Entry<String, JsonNode> e) -> {
            if (JsonNodeType.NULL == e.getValue().getNodeType()) {
                ((ObjectNode) source).remove(e.getKey());
            }

            if (JsonNodeType.OBJECT == e.getValue().getNodeType()) {

                clean(source.get(e.getKey()));

            }

            if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                ((ObjectNode) source).replace(e.getKey(), JsonNodeUtils.create(Streams.stream(e.getValue().elements()).map((JsonNode node) -> {
                    clean(node);
                    return node;
                }).filter((JsonNode node) -> JsonNodeType.NULL != node.getNodeType()).collect(Collectors.toList())));

                clean(source.get(e.getKey()));
            }
        });
    }

    public static void filter(JsonNode source, Consumer<Entry<String, JsonNode>> action) {

        if (source != null && JsonNodeType.OBJECT == source.getNodeType()) {
            Streams.stream(JsonNodeUtils.clone(source).fields()).forEach(action);
        }
    }

}
