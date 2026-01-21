package com.exemple.service.schema.description;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.schema.core.SchemaTestConfiguration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringJUnitConfig(SchemaTestConfiguration.class)
class SchemaDescriptionTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaDescription service;

    @Test
    void get() {

        // when perform get schema
        JsonNode schema = service.get("schema_test", "default", "default");

        // Then check response
        assertThat(schema.get("$schema").stringValue()).isEqualTo("https://json-schema.org/draft/2020-12/schema");

    }

    @Test
    void getNotExistSchema() throws IOException {

        // when perform get schema
        JsonNode schema = service.get("schema_test", "unknown", "unknown");

        // Then check response
        assertThat(schema).isEqualTo(MAPPER.readTree(new ClassPathResource("default-schema.json").getInputStream()));

    }

    @Test
    void getPatch() {

        // when perform get schema
        JsonNode schema = service.getPatch();

        // Then check response
        assertThat(schema.get("title").stringValue()).isEqualTo("JSON schema for JSONPatch files");

    }

}
