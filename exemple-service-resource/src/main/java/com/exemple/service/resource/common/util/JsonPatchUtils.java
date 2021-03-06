package com.exemple.service.resource.common.util;

import java.util.EnumSet;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.common.collect.Streams;

public final class JsonPatchUtils {

    public static final String OP = "op";

    public static final String PATH = "path";

    public static final String VALUE = "value";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonPatchUtils() {

    }

    public static ArrayNode diff(Map<String, Object> source, Map<String, Object> target) {

        return diff(MAPPER.convertValue(source, JsonNode.class), MAPPER.convertValue(target, JsonNode.class));
    }

    public static ArrayNode diff(JsonNode source, JsonNode target) {

        ArrayNode patchs = (ArrayNode) JsonDiff.asJson(source, target, EnumSet.of(DiffFlags.OMIT_COPY_OPERATION, DiffFlags.OMIT_MOVE_OPERATION));

        ArrayNode result = MAPPER.createArrayNode();

        Streams.stream(patchs.elements()).filter((JsonNode patch) -> !isRemoveOperation(patch)).flatMap(JsonPatchUtils::completePatch)
                .forEach(result::add);

        Streams.stream(patchs.elements()).filter(JsonPatchUtils::isRemoveOperation).forEach(result::add);

        return result;

    }

    public static boolean isRemoveOperation(JsonNode patch) {

        return "remove".equals(patch.get(OP).textValue());
    }

    private static Stream<JsonNode> completePatch(JsonNode element) {

        JsonNode value = element.path(VALUE);
        if (value.isObject()) {

            return Streams.stream(value.fields()).flatMap((Map.Entry<String, JsonNode> node) -> {
                ObjectNode patch = MAPPER.createObjectNode();
                patch.set(OP, element.get(OP));
                patch.set(PATH, MAPPER.convertValue(element.get(PATH).textValue() + JsonPointer.SEPARATOR + node.getKey(), JsonNode.class));
                patch.set(VALUE, node.getValue());
                return completePatch(patch);
            });

        }

        if (value.isArray()) {

            return IntStream.range(0, ((ArrayNode) value).size()).mapToObj((int index) -> {
                ObjectNode patch = MAPPER.createObjectNode();
                patch.set(OP, element.get(OP));
                patch.set(PATH, MAPPER.convertValue(element.get(PATH).textValue() + JsonPointer.SEPARATOR + index, JsonNode.class));
                patch.set(VALUE, ((ArrayNode) value).get(index));
                return patch;
            }).flatMap(JsonPatchUtils::completePatch);

        }

        return Stream.of(element);
    }

}
