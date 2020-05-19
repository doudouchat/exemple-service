package com.exemple.service.schema.description;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class SchemaDescriptionTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SchemaDescription service;

    @Test
    public void get() {

        JsonNode schema = service.get("default", "default", "schema_test", "default");

        assertThat(schema, is(notNullValue()));
        assertThat(schema, hasJsonField("$schema", "http://json-schema.org/draft-07/schema"));

    }

    @Test
    public void getPatch() {

        JsonNode schema = service.getPatch();

        assertThat(schema, is(notNullValue()));
        assertThat(schema, hasJsonField("title", "JSON schema for JSONPatch files"));

    }

}
