package com.exemple.service.resource.common.util;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.Jackson3JsonDiff;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import tools.jackson.core.JsonPointer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JsonPatchUtils {

    public static final String OP = "op";

    public static final String PATH = "path";

    public static final String VALUE = "value";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static ArrayNode diff(JsonNode source, JsonNode target) {

        ArrayNode patchs = (ArrayNode) Jackson3JsonDiff.asJson(source, target,
                EnumSet.of(DiffFlags.OMIT_COPY_OPERATION, DiffFlags.OMIT_MOVE_OPERATION));

        var result = MAPPER.createArrayNode();

        patchs.elements().stream().filter(Predicate.not(JsonPatchUtils::isRemoveOperation)).flatMap(JsonPatchUtils::completePatch)
                .forEach(result::add);

        patchs.elements().stream().filter(JsonPatchUtils::isRemoveOperation).forEach(result::add);

        return result;

    }

    public static <T extends JsonNode> boolean isRemoveOperation(T patch) {

        return "remove".equals(patch.get(OP).stringValue());
    }

    private static Stream<JsonNode> completePatch(JsonNode element) {

        JsonNode value = element.path(VALUE);
        if (value.isObject()) {

            return value.properties().stream().flatMap((Map.Entry<String, JsonNode> node) -> {
                var patch = MAPPER.createObjectNode();
                patch.set(OP, element.get(OP));
                patch.set(PATH, MAPPER.convertValue(element.get(PATH).stringValue() + JsonPointer.SEPARATOR + node.getKey(), JsonNode.class));
                patch.set(VALUE, node.getValue());
                return completePatch(patch);
            });

        }

        if (value.isArray()) {

            return IntStream.range(0, ((ArrayNode) value).size()).mapToObj((int index) -> {
                var patch = MAPPER.createObjectNode();
                patch.set(OP, element.get(OP));
                patch.set(PATH, MAPPER.convertValue(element.get(PATH).stringValue() + JsonPointer.SEPARATOR + index, JsonNode.class));
                patch.set(VALUE, ((ArrayNode) value).get(index));
                return patch;
            }).flatMap(JsonPatchUtils::completePatch);

        }

        return Stream.of(element);
    }

}
