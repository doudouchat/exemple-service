package com.exemple.service.resource.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import lombok.Builder;
import lombok.Getter;

public class JsonPatchUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void patchProperty() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");

        Map<String, Object> target = new HashMap<>();
        target.put("email", "jean.dupond@gmail.com");

        ArrayNode patch = JsonPatchUtils.diff(MAPPER.convertValue(model, JsonNode.class), MAPPER.convertValue(target, JsonNode.class));

        assertThat(patch.size(), is(1));
        assertThat(patch.get(0).get(JsonPatchUtils.OP).textValue(), is("replace"));
        assertThat(patch.get(0).get(JsonPatchUtils.PATH).textValue(), is("/email"));
        assertThat(patch.get(0).get(JsonPatchUtils.VALUE).textValue(), is("jean.dupond@gmail.com"));

    }

    @Test
    public void patchObject() {

        Map<String, Object> model = new HashMap<>();
        model.put("address", Address.builder().street("1 rue de la paix").build());

        Map<String, Object> target = new HashMap<>();
        target.put("address", Address.builder().city("Paris").build());

        ArrayNode patch = JsonPatchUtils.diff(MAPPER.convertValue(model, JsonNode.class), MAPPER.convertValue(target, JsonNode.class));
        List<JsonNode> result = sortPatchByPath(patch);

        assertThat(result.size(), is(2));
        assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("replace"));
        assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/address/city"));
        assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue(), is("Paris"));

        assertThat(result.get(1).get(JsonPatchUtils.OP).textValue(), is("replace"));
        assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue(), is("/address/street"));
        assertThat(result.get(1).get(JsonPatchUtils.VALUE).getNodeType(), is(JsonNodeType.NULL));

    }

    @Test
    public void patchMultiObject() {

        // setup build source

        Map<String, Object> addresses = new HashMap<>();
        addresses.put("home", Address.builder().street("1 rue de la paix").build());
        addresses.put("job", Address.builder().street("1 rue de la paix").city("Paris").build());

        JsonNode model = MAPPER.createObjectNode();
        ((ObjectNode) model).set("addresses", MAPPER.convertValue(addresses, JsonNode.class));

        // and build target

        addresses = new HashMap<>();
        addresses.put("home", Address.builder().street("1 rue de la paix").city("Paris").build());
        addresses.put("holidays", Address.builder().street("1 rue de la paix").city("Paris").build());

        JsonNode target = MAPPER.createObjectNode();
        ((ObjectNode) target).set("addresses", MAPPER.convertValue(addresses, JsonNode.class));

        // when perform diff

        ArrayNode patch = JsonPatchUtils.diff(model, target);
        List<JsonNode> result = sortPatchByPath(patch);

        // then check diffs

        assertThat(result.size(), is(4));

        assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add"));
        assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/addresses/holidays/city"));
        assertThat(result.get(0).get("value").textValue(), is("Paris"));

        assertThat(result.get(1).get(JsonPatchUtils.OP).textValue(), is("add"));
        assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue(), is("/addresses/holidays/street"));
        assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue(), is("1 rue de la paix"));

        assertThat(result.get(2).get(JsonPatchUtils.OP).textValue(), is("replace"));
        assertThat(result.get(2).get(JsonPatchUtils.PATH).textValue(), is("/addresses/home/city"));
        assertThat(result.get(2).get(JsonPatchUtils.VALUE).textValue(), is("Paris"));

        assertThat(result.get(3).get(JsonPatchUtils.OP).textValue(), is("remove"));
        assertThat(result.get(3).get(JsonPatchUtils.PATH).textValue(), is("/addresses/job"));
        assertThat(result.get(3).path(JsonPatchUtils.VALUE),
                is(MAPPER.convertValue(Address.builder().street("1 rue de la paix").city("Paris").build(), JsonNode.class)));

    }

    @Test
    public void patchArrayProperty() {

        Map<String, Object> model = new HashMap<>();

        Map<String, Object> target = new HashMap<>();

        List<String> preference = new ArrayList<>();
        preference.add("pref 1");

        target.put("preferences", preference);

        ArrayNode patch = JsonPatchUtils.diff(MAPPER.convertValue(model, JsonNode.class), MAPPER.convertValue(target, JsonNode.class));
        List<JsonNode> result = sortPatchByPath(patch);

        assertThat(result.size(), is(1));

        assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add"));
        assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/preferences/0"));
        assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue(), is("pref 1"));

    }

    @Test
    public void patchArrayObject() {

        Map<String, Object> model = new HashMap<>();

        Map<String, Object> target = new HashMap<>();

        List<Cgu> cgus = new ArrayList<>();
        cgus.add(Cgu.builder().code("code 1").version("version 1").build());

        target.put("cgus", cgus);

        ArrayNode patch = JsonPatchUtils.diff(MAPPER.convertValue(model, JsonNode.class), MAPPER.convertValue(target, JsonNode.class));
        List<JsonNode> result = sortPatchByPath(patch);

        assertThat(result.size(), is(2));

        assertThat(result.get(0).get(JsonPatchUtils.OP).textValue(), is("add"));
        assertThat(result.get(0).get(JsonPatchUtils.PATH).textValue(), is("/cgus/0/code"));
        assertThat(result.get(0).get(JsonPatchUtils.VALUE).textValue(), is("code 1"));

        assertThat(result.get(1).get(JsonPatchUtils.OP).textValue(), is("add"));
        assertThat(result.get(1).get(JsonPatchUtils.PATH).textValue(), is("/cgus/0/version"));
        assertThat(result.get(1).get(JsonPatchUtils.VALUE).textValue(), is("version 1"));

    }

    private List<JsonNode> sortPatchByPath(ArrayNode patch) {

        return Streams.stream(patch.elements()).sorted((n1, n2) -> n1.get("path").textValue().compareTo(n2.get("path").textValue()))
                .collect(Collectors.toList());
    }

    @Builder
    @Getter
    private static class Address {

        private final Object street;

        private final Object city;

    }

    @Builder
    @Getter
    private static class Cgu {

        private final Object code;

        private final Object version;

    }
}
