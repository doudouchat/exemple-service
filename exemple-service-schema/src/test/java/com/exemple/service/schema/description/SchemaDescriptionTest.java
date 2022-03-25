package com.exemple.service.schema.description;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

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
        assertAll(
                () -> assertThat(schema, is(notNullValue())),
                () -> assertThat(schema, hasJsonField("$schema", "http://json-schema.org/draft-07/schema")));

    }

    @Test
    public void getPatch() {

        // when perform get schema
        JsonNode schema = service.getPatch();

        // Then check response
        assertAll(
                () -> assertThat(schema, is(notNullValue())),
                () -> assertThat(schema, hasJsonField("title", "JSON schema for JSONPatch files")));

    }

}
