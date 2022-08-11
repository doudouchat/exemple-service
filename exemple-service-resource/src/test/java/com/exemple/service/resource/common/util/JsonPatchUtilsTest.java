package com.exemple.service.resource.common.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;

class JsonPatchUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void patchProperty() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"email\": \"jean.dupont@gmail\"}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"email\": \"jean.dupond@gmail\"}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // Then check diff
        assertAll(
                () -> assertThat(patch).hasSize(1),
                () -> assertThat(patch.get(0).get(JsonPatchUtils.OP).textValue()).isEqualTo("replace"),
                () -> assertThat(patch.get(0).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/email"),
                () -> assertThat(patch.get(0).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("jean.dupond@gmail"));

    }

    @Test
    void patchObject() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"address\": {\"street\": \"1 rue de la paix\"}}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"address\": {\"city\": \"Paris\"}}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // Then check diff
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue()).isEqualTo("add"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/address/city"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("Paris"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.OP).textValue()).isEqualTo("remove"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/address/street"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("1 rue de la paix"));

    }

    @Test
    void patchMultiObject() throws IOException {

        // Given build model
        JsonNode model = MAPPER
                .readTree(
                        "{\"addresses\": {\"home\": {\"street\": \"1 rue de la paix\", \"city\": null},\"job\": {\"street\": \"1 rue de la paix\", \"city\": \"Paris\"}}}");

        // And build target
        JsonNode target = MAPPER
                .readTree(
                        "{\"addresses\": {\"home\": {\"street\": \"1 rue de la paix\", \"city\": \"Paris\"},\"holidays\": {\"street\": \"1 rue de la paix\", \"city\": \"Paris\"}}}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // then check diffs
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result).hasSize(4),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue()).isEqualTo("add"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/addresses/holidays/city"),
                () -> assertThat(result.get(0).get("value").textValue()).isEqualTo("Paris"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.OP).textValue()).isEqualTo("add"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/addresses/holidays/street"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("1 rue de la paix"),
                () -> assertThat(result.get(2).get(JsonPatchUtils.OP).textValue()).isEqualTo("replace"),
                () -> assertThat(result.get(2).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/addresses/home/city"),
                () -> assertThat(result.get(2).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("Paris"),
                () -> assertThat(result.get(3).get(JsonPatchUtils.OP).textValue()).isEqualTo("remove"),
                () -> assertThat(result.get(3).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/addresses/job"),
                () -> assertThat(result.get(3).path(JsonPatchUtils.VALUE))
                        .isEqualTo(MAPPER.readTree("{\"street\": \"1 rue de la paix\", \"city\": \"Paris\"}")));

    }

    @Test
    void patchArrayProperty() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"preferences\":[]}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"preferences\":[\"pref 1\"]}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // then check diffs
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result).hasSize(1),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue()).isEqualTo("add"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/preferences/0"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("pref 1"));

    }

    @Test
    void patchArrayObject() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"cgus\": []}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"cgus\": [{\"code\": \"code 1\", \"version\": \"version 1\"}]}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // then check diffs
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result).hasSize(2),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue()).isEqualTo("add"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/cgus/0/code"),
                () -> assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("code 1"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.OP).textValue()).isEqualTo("add"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue()).isEqualTo("/cgus/0/version"),
                () -> assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue()).isEqualTo("version 1"));

    }

    private List<JsonNode> sortPatchByPath(ArrayNode patch) {

        return Streams.stream(patch.elements()).sorted((n1, n2) -> n1.get("path").textValue().compareTo(n2.get("path").textValue()))
                .collect(Collectors.toList());
    }
}
