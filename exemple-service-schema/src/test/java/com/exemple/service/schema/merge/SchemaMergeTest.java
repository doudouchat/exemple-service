package com.exemple.service.schema.merge;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.JsonNodeUtils;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class SchemaMergeTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SchemaMerge service;

    @Autowired
    private SchemaResource schemaResource;

    @Test
    public void simpleField() {

        // Given source

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> data = new HashMap<>();
            data.put("field1", "new_value1");
            return data;
        });

        // And original source

        JsonNode originalSource = JsonNodeUtils.create(() -> {

            Map<String, Object> data = new HashMap<>();
            data.put("field1", "value1");
            data.put("field2", "value2");
            data.put("field3", "value3");

            return data;
        });

        // And mock schema fields

        Set<String> fields = new HashSet<>();
        fields.add("field1");
        fields.add("field2");

        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFields(fields);
        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(resourceSchema);

        // When perform merge

        service.mergeMissingFieldFromOriginal(app, "default", "schema_test", "default", source, originalSource);

        // Then check source

        assertThat(source, hasJsonField("field1", "new_value1"));
        assertThat(source, not(hasJsonField("field2")));
        assertThat(source, hasJsonField("field3", "value3"));

    }

    @Test
    public void simpleMap() {

        // Given source

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> data = new HashMap<>();

            Map<String, Object> object = new HashMap<>();
            object.put("object1", "new_value1");

            data.put("field1", object);

            return data;
        });

        // And original source

        JsonNode originalSource = JsonNodeUtils.create(() -> {

            Map<String, Object> data = new HashMap<>();

            Map<String, Object> object = new HashMap<>();
            object.put("object1", "value1");
            object.put("object2", "value2");
            object.put("object3", "value3");

            data.put("field1", object);

            return data;
        });

        // And mock schema fields

        Set<String> fields = new HashSet<>();
        fields.add("field1[object1,object2]");

        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFields(fields);
        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(resourceSchema);

        // When perform merge

        service.mergeMissingFieldFromOriginal(app, "default", "schema_test", "default", source, originalSource);

        // Then check source

        assertThat(source, hasJsonField("field1", hasJsonField("object1", "new_value1")));
        assertThat(source, hasJsonField("field1", not(hasJsonField("object2"))));
        assertThat(source, hasJsonField("field1", hasJsonField("object3", "value3")));

    }

    @Test
    public void complexeMap() {

        // Given source

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> data = new HashMap<>();

            Map<String, Object> objectValue = new HashMap<>();
            objectValue.put("obj_field1", "new_value1");

            Map<String, Object> object = new HashMap<>();
            object.put("object1", objectValue);

            data.put("field1", object);

            return data;
        });

        // And original source

        JsonNode originalSource = JsonNodeUtils.create(() -> {

            Map<String, Object> data = new HashMap<>();

            Map<String, Object> objectValue1 = new HashMap<>();
            objectValue1.put("obj_field1", "value1");
            objectValue1.put("obj_field2", "value2");

            Map<String, Object> objectValue2 = new HashMap<>();
            objectValue2.put("obj_field1", "value1");
            objectValue2.put("obj_field2", "value2");

            Map<String, Object> objectValue3 = new HashMap<>();
            objectValue3.put("obj_field1", "value1");
            objectValue3.put("obj_field2", "value2");

            Map<String, Object> object = new HashMap<>();
            object.put("object1", objectValue1);
            object.put("object2", objectValue2);
            object.put("object3", objectValue3);

            data.put("field1", object);

            return data;
        });

        // And mock schema fields

        Set<String> fields = new HashSet<>();
        fields.add("field1[object1[obj_field1],object2[obj_field1]]");

        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFields(fields);
        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(resourceSchema);

        // When perform merge

        service.mergeMissingFieldFromOriginal(app, "default", "schema_test", "default", source, originalSource);

        // Then check source

        assertThat(source, hasJsonField("field1", hasJsonField("object1", hasJsonField("obj_field1", "new_value1"))));
        assertThat(source, hasJsonField("field1", hasJsonField("object1", hasJsonField("obj_field2", "value2"))));
        assertThat(source, hasJsonField("field1", not(hasJsonField("object2"))));
        assertThat(source, hasJsonField("field1", hasJsonField("object3", hasJsonField("obj_field1", "value1"))));
        assertThat(source, hasJsonField("field1", hasJsonField("object3", hasJsonField("obj_field2", "value2"))));

    }
}
