package com.exemple.service.api.common.json;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonUtilsTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void merge() throws IOException {

        // Given build account
        JsonNode source = MAPPER.readTree(
                """
                {"lastname" : "dupond", "firstname" : "jean"}
                """);

        // And build override
        JsonNode override = MAPPER.readTree(
                """
                {"lastname" : "dupont", "id" : "123"}
                """);

        // When perform merge
        JsonNode sourceOverride = JsonUtils.merge(source, override);

        // Then check merge source
        JsonNode expectedResult = MAPPER.readTree(
                """
                {"lastname" : "dupont", "firstname" : "jean", "id" : "123"}
                """);
        assertThat(sourceOverride).isEqualTo(expectedResult);

    }

}
