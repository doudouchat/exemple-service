package com.exemple.service.resource.common.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonNodeFilterUtils {

    public static JsonNode clean(JsonNode source) {

        BiPredicate<JsonNode, Entry<String, JsonNode>> predicate = (root, node) -> !node.getValue().isNull();

        return filter(source, predicate);
    }

    public static JsonNode filter(JsonNode source, BiPredicate<JsonNode, Entry<String, JsonNode>> predicate) {
        return filter(Maps.immutableEntry(null, source), new ObjectMapper().createObjectNode(), predicate);
    }

    private static JsonNode filter(Entry<String, JsonNode> source, ObjectNode root, BiPredicate<JsonNode, Entry<String, JsonNode>> predicate) {

        if (source.getValue().isObject()) {

            Streams.stream(source.getValue().fields()).filter(node -> predicate.test(source.getValue(), node))
                    .forEach(node -> root.set(node.getKey(), filter(node, new ObjectMapper().createObjectNode(), predicate)));

            return root;
        }

        if (source.getValue().isArray()) {

            var arrayNode = JsonNodeFactory.instance.arrayNode();
            List<JsonNode> nodes = Streams.stream(source.getValue().elements())
                    .filter(node -> predicate.test(source.getValue(), Maps.immutableEntry(null, node)))
                    .map(node -> filter(Maps.immutableEntry(null, node), new ObjectMapper().createObjectNode(), predicate))
                    .collect(Collectors.toList());
            arrayNode.addAll(nodes);

            return arrayNode;
        }

        root.set(source.getKey(), source.getValue());

        return root.get(source.getKey());
    }

}
