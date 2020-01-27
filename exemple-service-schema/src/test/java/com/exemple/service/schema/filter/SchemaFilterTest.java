package com.exemple.service.schema.filter;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

import java.util.Collections;
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
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { SchemaTestConfiguration.class })
public class SchemaFilterTest extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaFilter schemaFilter;

    @Autowired
    private SchemaResource schemaResource;

    @Test
    public void filter() {

        Map<String, Object> data = new HashMap<>();
        data.put("field1", "value1");
        data.put("field2", "value2");

        Map<String, Object> object = new HashMap<>();
        object.put("object1", "value1");
        object.put("object2", "value2");

        data.put("field3", object);

        Set<String> filters = new HashSet<>();
        filters.add("field1");
        filters.add("field3[object2]");

        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFilters(filters);
        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"))).thenReturn(resourceSchema);

        JsonNode newData = schemaFilter.filter(app, "default", "schema_test", MAPPER.convertValue(data, JsonNode.class));
        assertThat(newData, hasJsonField("field1", "value1"));
        assertThat(newData, not(hasJsonField("field2")));
        assertThat(newData, hasJsonField("field3", hasJsonField("object2", "value2")));

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void filterFailure() {

        Map<String, Object> data = new HashMap<>();
        data.put("field1", "value1");

        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFilters(Collections.singleton(" ,field1"));

        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"))).thenReturn(resourceSchema);

        schemaFilter.filter(app, "default", "schema_test", MAPPER.convertValue(data, JsonNode.class));

    }

}
