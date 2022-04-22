package com.exemple.service.schema.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.core.SchemaTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(SchemaTestConfiguration.class)
public class SchemaFilterTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaFilter schemaFilter;

    @Autowired
    private SchemaResource schemaResource;

    @Test
    public void filter() throws IOException {

        // Given filter configuration
        Set<String> filters = new HashSet<>();
        filters.add("field1");
        filters.add("field3[object2]");

        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFilters(filters);
        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(Optional.of(resourceSchema));

        // And create source
        JsonNode source = MAPPER
                .readTree("{\"field1\": \"value1\", \"field2\": \"value2\",  \"field3\": {\"object1\": \"value1\", \"object2\": \"value2\"}}");

        // When perform filter
        JsonNode newSource = schemaFilter.filter(app, "default", "schema_test", "default", source);

        // Then check source after filter
        assertThat(newSource).isEqualTo(MAPPER.readTree("{\"field1\": \"value1\", \"field3\": {\"object2\": \"value2\"}}"));
    }

    @Test
    public void filterFailure() throws IOException {

        // Given filter configuration
        String app = RandomStringUtils.random(15);
        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setFilters(Collections.singleton(" ,field1"));

        Mockito.when(schemaResource.get(Mockito.eq(app), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(Optional.of(resourceSchema));

        // And create source
        JsonNode source = MAPPER.readTree("{\"field1\": \"value1\"}");

        // When perform
        Throwable throwable = catchThrowable(() -> schemaFilter.filter(app, "default", "schema_test", "default", source));

        // Then check throwable
        assertThat(throwable).isInstanceOf(IllegalArgumentException.class);

    }

}
