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
public class SchemaFilterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaFilter schemaFilter;

    @Test
    public void filter() throws IOException {

        // Given create source
        JsonNode source = MAPPER
                .readTree(
                        "{\"lastname\": \"jean\", \"password\": \"value\",  \"addresses\": {\"holiday\": {\"street\": \"Paris\", \"country\": \"French\"}}}");

        // When perform filter
        JsonNode newSource = schemaFilter.filter("default", "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{\"lastname\": \"jean\", \"addresses\": {\"holiday\": {\"street\": \"Paris\"}}}"));
    }

    @Test
    public void filterWhenSchemaNotExists() throws IOException {

        // Given create source
        JsonNode source = MAPPER
                .readTree(
                        "{\"lastname\": \"jean\", \"password\": \"value\",  \"addresses\": {\"holiday\": {\"street\": \"Paris\", \"country\": \"French\"}}}");

        // When perform filter
        JsonNode newSource = schemaFilter.filter("other", "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{}"));
    }

    @Test
    public void filterAllProperties() throws IOException {

        // Given create source
        JsonNode source = MAPPER
                .readTree(
                        "{\"lastname\": \"jean\", \"hide\": \"value2\",  \"addresses\": {\"holiday\": {\"street\": \"Paris\", \"country\": \"French\"}}}");

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllProperties("default", "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{\"lastname\": \"jean\", \"addresses\": {\"holiday\": {\"street\": \"Paris\"}}}"));
    }

    @Test
    public void filterAllPropertiesWhenSchemaNotExists() throws IOException {

        // Given create source
        JsonNode source = MAPPER
                .readTree(
                        "{\"lastname\": \"jean\", \"hide\": \"value2\",  \"addresses\": {\"holiday\": {\"street\": \"Paris\", \"country\": \"French\"}}}");

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllProperties("other", "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{}"));
    }

    private static Stream<Arguments> filterAllAdditionalProperties() throws IOException {

        JsonNode source1 = MAPPER
                .readTree(
                        "{\"lastname\": \"jean\", \"hide\": \"value2\",  \"addresses\": {\"holiday\": {\"city\": \"Paris\", \"street\": \"5th Avenue\", \"country\": \"French\"}}}");
        JsonNode sourceExpected1 = MAPPER.readTree("{\"hide\": \"value2\"}");

        JsonNode source2 = MAPPER
                .readTree(
                        "{\"lastname\": \"jean\", \"addresses\": {\"holiday\": {\"city\": \"Paris\", \"street\": \"5th Avenue\", \"country\": \"French\"}}}");
        JsonNode sourceExpected2 = MAPPER.readTree("{}");

        JsonNode source3 = MAPPER.readTree("{\"lastname\": \"jean\", \"firstname\": \"dudpond\"}");
        JsonNode sourceExpected3 = MAPPER.readTree("{}");

        return Stream.of(
                Arguments.of(source1, sourceExpected1),
                Arguments.of(source2, sourceExpected2),
                Arguments.of(source3, sourceExpected3));
    }

    @ParameterizedTest
    @MethodSource
    public void filterAllAdditionalProperties(JsonNode source, JsonNode expectedSource) {

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllAdditionalProperties("default", "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(expectedSource);
    }

    @Test
    public void filterAllAdditionalPropertiesWhenSchemaNotExists() throws IOException {

        // Given create source
        JsonNode source = MAPPER.readTree("{\"lastname\": \"jean\", \"addresses\": {\"holiday\": {\"city\": \"Paris\"}}}");

        // When perform filter
        JsonNode newSource = schemaFilter.filterAllAdditionalProperties("other", "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(source);
    }

}
