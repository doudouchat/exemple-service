package com.exemple.service.resource.common.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class JsonPatchUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void patchProperty() {

        // Given build model
        var source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail"}
                """);

        // And build target
        var target = MAPPER.readTree(
                """
                {"email": "jean.dupond@gmail"}
                """);

        // when perform diff
        var patch = JsonPatchUtils.diff(source, target);

        // Then check diff
        assertThat(patch).isEqualTo(MAPPER.readTree(
                """
                [{"op": "replace", "path": "/email", "value": "jean.dupond@gmail"}]
                """));

    }

    @Test
    void patchObject() {

        // Given build model
        var source = MAPPER.readTree(
                """
                {"address": {"street": "1 rue de la paix"}}
                """);

        // And build target
        var target = MAPPER.readTree(
                """
                {"address": {"city": "Paris"}}
                """);

        // when perform diff
        var patch = JsonPatchUtils.diff(source, target);

        // Then check diff
        assertThat(patch).isEqualTo(MAPPER.readTree(
                """
                [
                  {"op": "add", "path": "/address/city", "value": "Paris"},
                  {"op": "remove", "path": "/address/street", "value": "1 rue de la paix"}
                ]
                """));

    }

    @Test
    void patchMultiObject() {

        // Given build model
        var source = MAPPER.readTree(
                """
                {"addresses": {"home": {"street": "1 rue de la paix", "city": null},"job": {"street": "1 rue de la paix", "city": "Paris"}}}
                """);

        // And build target
        var target = MAPPER.readTree(
                """
                {"addresses": {"home": {"street": "1 rue de la paix", "city": "Paris"},"holidays": {"street": "1 rue de la paix", "city": "Paris"}}}
                """);

        // when perform diff
        var patch = JsonPatchUtils.diff(source, target);

        // then check diffs
        assertThat(patch).isEqualTo(MAPPER.readTree(
                """
                [
                  {"op":"replace","path":"/addresses/home/city","value":"Paris"},
                  {"op":"add","path":"/addresses/holidays/street","value":"1 rue de la paix"},
                  {"op":"add","path":"/addresses/holidays/city","value":"Paris"},
                  {"op":"remove","path":"/addresses/job","value":{"street":"1 rue de la paix","city":"Paris"}}
                ]
                """));
    }

    @Test
    void patchArrayProperty() {

        // Given build model
        var source = MAPPER.readTree(
                """
                {"preferences":[]}
                """);

        // And build target
        var target = MAPPER.readTree(
                """
                {"preferences":["pref 1"]}
                """);

        // when perform diff
        var patch = JsonPatchUtils.diff(source, target);

        // then check diffs
        assertThat(patch).isEqualTo(MAPPER.readTree(
                """
                [
                  {"op": "add", "path": "/preferences/0", "value": "pref 1"}
                ]
                """));

    }

    @Test
    void patchArrayObject() {

        // Given build model
        var source = MAPPER.readTree(
                """
                {"cgus": []}
                """);

        // And build target
        var target = MAPPER.readTree(
                """
                {"cgus": [{"code": "code 1", "version": "version 1"}]}
                """);

        // when perform diff
        var patch = JsonPatchUtils.diff(source, target);

        // then check diffs
        assertThat(patch).isEqualTo(MAPPER.readTree(
                """
                [
                  {"op": "add", "path": "/cgus/0/code", "value": "code 1"},
                  {"op": "add", "path": "/cgus/0/version", "value": "version 1"}
                ]
                """));

    }
}
