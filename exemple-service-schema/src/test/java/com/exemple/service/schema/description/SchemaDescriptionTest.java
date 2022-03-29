package com.exemple.service.schema.description;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@SpringJUnitConfig(SchemaTestConfiguration.class)
public class SchemaDescriptionTest {

    @Autowired
    private SchemaDescription service;

    @Test
    public void get() {

        // when perform get schema
        JsonNode schema = service.get("default", "default", "schema_test", "default");

        // Then check response
        assertThat(schema.get("$schema")).hasToString("\"http://json-schema.org/draft-07/schema\"");

    }

    @Test
    public void getPatch() {

        // when perform get schema
        JsonNode schema = service.getPatch();

        // Then check response
        assertThat(schema.get("title")).hasToString("\"JSON schema for JSONPatch files\"");

    }

}
