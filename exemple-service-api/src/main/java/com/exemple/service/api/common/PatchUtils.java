package com.exemple.service.api.common;

import java.util.function.BinaryOperator;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatch;
import com.google.common.collect.Streams;

public final class PatchUtils {

    private PatchUtils() {

    }

    public static JsonNode diff(JsonNode patch, JsonNode source) {

        JsonNode target = JsonPatch.apply(patch, source);

        BinaryOperator<JsonNode> function = (n1, n2) -> n2;

        return Streams.stream(patch.elements()).reduce(JsonNodeUtils.init(), (JsonNode root, JsonNode p) -> {

            diff(root, target, JsonPointer.compile(p.get("path").textValue()));

            if ("move".equals(p.path("op").textValue())) {
                diff(root, target, JsonPointer.compile(p.get("from").textValue()));

            }

            return root;
        }, function);

    }

    private static void diff(JsonNode root, JsonNode target, JsonPointer pointer) {

        JsonNode node = target.path(pointer.getMatchingProperty());
        if (node.isObject()) {
            JsonPointer p1 = JsonPointer.compile("/".concat(pointer.getMatchingProperty()));
            JsonPointer p2 = JsonPointer.compile("/".concat(pointer.tail().getMatchingProperty()));

            if (root.path(pointer.getMatchingProperty()).isMissingNode()) {
                JsonNodeUtils.set(root, JsonNodeUtils.init(), pointer.getMatchingProperty());
            }
            node = target.at(p1.append(p2));
            if (!p2.getMatchingProperty().isEmpty()) {
                JsonNodeUtils.set(root.get(pointer.getMatchingProperty()), node.isMissingNode() ? null : node, p2.getMatchingProperty());
            } else {
                JsonNodeUtils.set(root, target.at(p1), p1.getMatchingProperty());
            }

        } else {
            JsonNodeUtils.set(root, node.isMissingNode() ? null : node, pointer.getMatchingProperty());
        }

    }

}
