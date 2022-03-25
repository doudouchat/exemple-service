package com.exemple.service.resource.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;

public class JsonPatchUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void patchProperty() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"email\": \"jean.dupont@gmail\"}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"email\": \"jean.dupond@gmail\"}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // Then check diff
        assertAll(
                () -> assertThat(patch.size(), is(1)),
                () -> assertThat(patch.get(0).get(JsonPatchUtils.OP).textValue(), is("replace")),
                () -> assertThat(patch.get(0).get(JsonPatchUtils.PATH).textValue(), is("/email")),
                () -> assertThat(patch.get(0).get(JsonPatchUtils.VALUE).textValue(), is("jean.dupond@gmail")));

    }

    @Test
    public void patchObject() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"address\": {\"street\": \"1 rue de la paix\"}}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"address\": {\"city\": \"Paris\"}}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // Then check diff
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result.size(), is(2)),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/address/city")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue(), is("Paris")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.OP).textValue(), is("remove")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue(), is("/address/street")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue(), is("1 rue de la paix")));

    }

    @Test
    public void patchMultiObject() throws IOException {

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
                () -> assertThat(result.size(), is(4)),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/addresses/holidays/city")),
                () -> assertThat(result.get(0).get("value").textValue(), is("Paris")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.OP).textValue(), is("add")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue(), is("/addresses/holidays/street")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue(), is("1 rue de la paix")),
                () -> assertThat(result.get(2).get(JsonPatchUtils.OP).textValue(), is("replace")),
                () -> assertThat(result.get(2).get(JsonPatchUtils.PATH).textValue(), is("/addresses/home/city")),
                () -> assertThat(result.get(2).get(JsonPatchUtils.VALUE).textValue(), is("Paris")),
                () -> assertThat(result.get(3).get(JsonPatchUtils.OP).textValue(), is("remove")),
                () -> assertThat(result.get(3).get(JsonPatchUtils.PATH).textValue(), is("/addresses/job")),
                () -> assertThat(result.get(3).path(JsonPatchUtils.VALUE),
                        is(MAPPER.readTree("{\"street\": \"1 rue de la paix\", \"city\": \"Paris\"}"))));

    }

    @Test
    public void patchArrayProperty() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"preferences\":[]}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"preferences\":[\"pref 1\"]}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // then check diffs
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result.size(), is(1)),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/preferences/0")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue(), is("pref 1")));

    }

    @Test
    public void patchArrayObject() throws IOException {

        // Given build model
        JsonNode model = MAPPER.readTree("{\"cgus\": []}");

        // And build target
        JsonNode target = MAPPER.readTree("{\"cgus\": [{\"code\": \"code 1\", \"version\": \"version 1\"}]}");

        // when perform diff
        ArrayNode patch = JsonPatchUtils.diff(model, target);

        // then check diffs
        List<JsonNode> result = sortPatchByPath(patch);
        assertAll(
                () -> assertThat(result.size(), is(2)),
                () -> assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/cgus/0/code")),
                () -> assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue(), is("code 1")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.OP).textValue(), is("add")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue(), is("/cgus/0/version")),
                () -> assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue(), is("version 1")));

    }

    private List<JsonNode> sortPatchByPath(ArrayNode patch) {

        return Streams.stream(patch.elements()).sorted((n1, n2) -> n1.get("path").textValue().compareTo(n2.get("path").textValue()))
                .collect(Collectors.toList());
    }
}
