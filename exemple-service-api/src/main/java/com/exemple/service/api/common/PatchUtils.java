package com.exemple.service.api.common;

import java.util.function.BinaryOperator;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.flipkart.zjsonpatch.JsonPatch;

public final class PatchUtils {

    private PatchUtils() {

    }

    public static JsonNode diff(JsonNode patch, JsonNode source) {

        JsonNode target = JsonPatch.apply(patch, source);

        BinaryOperator<JsonNode> function = (n1, n2) -> n2;

        return JsonNodeUtils.stream(patch.elements()).reduce(JsonNodeUtils.init(), (JsonNode root, JsonNode p) -> {

            diff(root, target, JsonPointer.compile(p.get("path").textValue()));

            if ("move".equals(p.path("op").textValue())) {
                diff(root, target, JsonPointer.compile(p.get("from").textValue()));

            }

            return root;
        }, function);

    }

    private static void diff(JsonNode root, JsonNode target, JsonPointer pointer) {

        JsonNode node = target.path(pointer.getMatchingProperty());
        if (JsonNodeType.OBJECT == node.getNodeType()) {
            JsonPointer p1 = JsonPointer.compile("/".concat(pointer.getMatchingProperty()));
            JsonPointer p2 = JsonPointer.compile("/".concat(pointer.tail().getMatchingProperty()));

            if (JsonNodeType.MISSING == root.path(pointer.getMatchingProperty()).getNodeType()) {
                JsonNodeUtils.set(root, JsonNodeUtils.init(), pointer.getMatchingProperty());
            }
            node = target.at(p1.append(p2));
            if (!p2.getMatchingProperty().isEmpty()) {
                JsonNodeUtils.set(root.get(pointer.getMatchingProperty()), JsonNodeType.MISSING == node.getNodeType() ? null : node,
                        p2.getMatchingProperty());
            } else {
                JsonNodeUtils.set(root, target.at(p1), p1.getMatchingProperty());
            }

        } else {
            JsonNodeUtils.set(root, JsonNodeType.MISSING == node.getNodeType() ? null : node, pointer.getMatchingProperty());
        }

    }

}
