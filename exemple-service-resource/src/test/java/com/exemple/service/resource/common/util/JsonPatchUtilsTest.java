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
import com.google.common.collect.Streams;

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
        model.put("address", new Address("1 rue de la paix", null));

        Map<String, Object> target = new HashMap<>();
        target.put("address", new Address(null, "Paris"));

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

        Map<String, Object> model = new HashMap<>();

        Map<String, Object> addresses = new HashMap<>();
        addresses.put("home", new Address("1 rue de la paix", null));
        addresses.put("job", new Address("1 rue de la paix", "Paris"));

        model.put("addresses", addresses);

        Map<String, Object> target = new HashMap<>();

        addresses = new HashMap<>();
        addresses.put("home", new Address("1 rue de la paix", "Paris"));
        addresses.put("holidays", new Address("1 rue de la paix", "Paris"));

        target.put("addresses", addresses);

        ArrayNode patch = JsonPatchUtils.diff(MAPPER.convertValue(model, JsonNode.class), MAPPER.convertValue(target, JsonNode.class));
        List<JsonNode> result = sortPatchByPath(patch);

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
        assertThat(result.get(3).path(JsonPatchUtils.VALUE), is(JsonNodeUtils.create(new Address("1 rue de la paix", "Paris"))));

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
        cgus.add(new Cgu("code 1", "version 1"));

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

    private static class Address {

        private Object street;

        private Object city;

        public Address(Object street, Object city) {
            this.street = street;
            this.city = city;
        }

        @SuppressWarnings("unused")
        public Object getStreet() {
            return street;
        }

        @SuppressWarnings("unused")
        public void setStreet(Object street) {
            this.street = street;
        }

        @SuppressWarnings("unused")
        public Object getCity() {
            return city;
        }

        @SuppressWarnings("unused")
        public void setCity(Object city) {
            this.city = city;
        }

    }

    private static class Cgu {

        private Object code;

        private Object version;

        public Cgu(Object code, Object version) {
            this.code = code;
            this.version = version;
        }

        @SuppressWarnings("unused")
        public Object getCode() {
            return code;
        }

        @SuppressWarnings("unused")
        public void setCode(Object code) {
            this.code = code;
        }

        @SuppressWarnings("unused")
        public Object getVersion() {
            return version;
        }

        @SuppressWarnings("unused")
        public void setVersion(Object version) {
            this.version = version;
        }

    }

}
