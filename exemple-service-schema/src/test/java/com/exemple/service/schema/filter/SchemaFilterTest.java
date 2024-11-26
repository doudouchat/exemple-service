package com.exemple.service.schema.filter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(SchemaTestConfiguration.class)
class SchemaFilterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaFilter schemaFilter;

    @Test
    void filter() throws IOException {

        // Given create source
        JsonNode source = MAPPER.readTree(
                """
                {"lastname": "jean", "password": "value", "update_date": "2022-05-23",  "addresses": {"holiday": {"street": "Paris", "country": "French"}}}
                """);

        // When perform filter
        JsonNode newSource = schemaFilter.filter("schema_test", "default", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree(
                """
                {"lastname": "jean", "update_date": "2022-05-23", "addresses": {"holiday": {"street": "Paris"}}}
                """));
    }

    @Test
    void filterWhenSchemaNotExists() throws IOException {

        // Given create source
        JsonNode source = MAPPER.readTree(
                """
                {"lastname": "jean", "password": "value", "update_date": "2022-05-23", "addresses": {"holiday": {"street": "Paris", "country": "French"}}}
                """);

        // When perform filter
        JsonNode newSource = schemaFilter.filter("other_test", "default", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{}"));
    }

    @Test
    void filterAllProperties() throws IOException {

        // Given create source
        JsonNode source = MAPPER.readTree(
                """
                {"lastname": "jean", "hide": "value2", "update_date": "2022-05-23", "addresses": {"holiday": {"street": "Paris", "country": "French"}}}
                """);

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllProperties("schema_test", "default", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree(
                """
                {"lastname": "jean", "update_date": "2022-05-23", "addresses": {"holiday": {"street": "Paris"}}}
                """));
    }

    @Test
    void filterAllPropertiesWhenSchemaNotExists() throws IOException {

        // Given create source
        JsonNode source = MAPPER.readTree(
                """
                {"lastname": "jean", "hide": "value2",  "update_date": "2022-05-23", "addresses": {"holiday": {"street": "Paris", "country": "French"}}}
                """);

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllProperties("other_test", "default", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{}"));
    }

    static Stream<Arguments> filterAllAdditionalProperties() throws IOException {

        JsonNode source1 = MAPPER.readTree(
                """
                {"lastname": "jean", "hide": "value2",  "addresses": {"holiday": {"city": "Paris", "street": "5th Avenue", "country": "French"}}}
                """);
        JsonNode sourceExpected1 = MAPPER.readTree(
                """
                {"hide": "value2"}"
                """);

        JsonNode source2 = MAPPER.readTree(
                """
                {"lastname": "jean", "addresses": {"holiday": {"city": "Paris", "street": "5th Avenue", "country": "French"}}}
                """);
        JsonNode sourceExpected2 = MAPPER.readTree("{}");

        JsonNode source3 = MAPPER.readTree(
                """
                {"lastname": "jean", "firstname": "dudpond"}
                """);
        JsonNode sourceExpected3 = MAPPER.readTree("{}");

        return Stream.of(
                Arguments.of(source1, sourceExpected1),
                Arguments.of(source2, sourceExpected2),
                Arguments.of(source3, sourceExpected3));
    }

    @ParameterizedTest
    @MethodSource
    void filterAllAdditionalProperties(JsonNode source, JsonNode expectedSource) {

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllAdditionalProperties("schema_test", "default", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(expectedSource);
    }

    @Test
    void filterAllAdditionalPropertiesWhenSchemaNotExists() throws IOException {

        // Given create source
        JsonNode source = MAPPER.readTree(
                """
                {"lastname": "jean", "addresses": {"holiday": {"city": "Paris"}}}
                """);

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllAdditionalProperties("other_test", "default", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(source);
    }

}
