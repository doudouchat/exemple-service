package com.exemple.service.schema.description;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(SchemaTestConfiguration.class)
class SchemaDescriptionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaDescription service;

    @Test
    void get() {

        // when perform get schema
        JsonNode schema = service.get("default", "default", "schema_test", "default");

        // Then check response
        assertThat(schema.get("$schema").textValue()).isEqualTo("http://json-schema.org/draft-07/schema");

    }

    @Test
    void getNotExistSchema() throws IOException {

        // when perform get schema
        JsonNode schema = service.get("unknown", "unknown", "schema_test", "unknown");

        // Then check response
        assertThat(schema).isEqualTo(MAPPER.readTree(new ClassPathResource("default-schema.json").getInputStream()));

    }

    @Test
    void getPatch() {

        // when perform get schema
        JsonNode schema = service.getPatch();

        // Then check response
        assertThat(schema.get("title").textValue()).isEqualTo("JSON schema for JSONPatch files");

    }

}
