package com.exemple.service.resource.common.util;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiPredicate;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonNodeFilterUtils {

    public static JsonNode clean(JsonNode source) {

        BiPredicate<JsonNode, Entry<String, JsonNode>> predicate = (root, node) -> !node.getValue().isNull();

        return filter(source, predicate);
    }

    public static JsonNode filter(JsonNode source, BiPredicate<JsonNode, Entry<String, JsonNode>> predicate) {
        return filter(Maps.immutableEntry("", source), new ObjectMapper().createObjectNode(), predicate);
    }

    private static JsonNode filter(Entry<String, JsonNode> source, ObjectNode root, BiPredicate<JsonNode, Entry<String, JsonNode>> predicate) {

        if (source.getValue().isObject()) {

            source.getValue().properties().stream().filter(node -> predicate.test(source.getValue(), node))
                    .forEach(node -> root.set(node.getKey(), filter(node, new ObjectMapper().createObjectNode(), predicate)));

            return root;
        }

        if (source.getValue().isArray()) {

            var arrayNode = JsonNodeFactory.instance.arrayNode();
            List<JsonNode> nodes = source.getValue().valueStream()
                    .filter(node -> predicate.test(source.getValue(), Maps.immutableEntry(source.getKey(), node)))
                    .map(node -> filter(Maps.immutableEntry(source.getKey(), node), new ObjectMapper().createObjectNode(), predicate))
                    .toList();
            arrayNode.addAll(nodes);

            return arrayNode;
        }

        root.set(source.getKey(), source.getValue());

        return root.get(source.getKey());
    }

}
