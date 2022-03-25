package com.exemple.service.resource.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.schema.impl.SchemaResourceImpl;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(ResourceTestConfiguration.class)
public class SchemaResourceTest {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaResource.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource resource;

    @BeforeEach
    public void resetResourceContext() {

        ResourceExecutionContext.destroy();

    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveSchema {

        private JsonNode schemaResource;

        @Test
        @Order(1)
        public void save() throws IOException {

            // Given build schema

            schemaResource = MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("test.json").getInputStream()));

            SchemaEntity resourceSchema = new SchemaEntity();
            resourceSchema.setApplication("app1");
            resourceSchema.setVersion("v1");
            resourceSchema.setResource("account");
            resourceSchema.setProfile("example");
            resourceSchema.setContent(schemaResource);
            resourceSchema.setFilters(Collections.singleton("filter"));
            resourceSchema.setFields((Collections.singleton("field")));
            resourceSchema.setPatchs(Collections.singleton(MAPPER.convertValue(Collections.singletonMap("op", "add"), JsonNode.class)));

            // When perform save

            resource.save(resourceSchema);

            // Then check schema

            JsonNode schemaResource = resource.get("app1", "v1", "account", "example").getContent();

            assertThat(schemaResource, not(nullValue()));

            LOG.debug(schemaResource.toPrettyString());

            assertThat(schemaResource, is(this.schemaResource));

            // And check filter
            Set<String> filter = resource.get("app1", "v1", "account", "example").getFilters();
            assertThat(filter, hasItem("filter"));

            // And check fields
            Set<String> fields = resource.get("app1", "v1", "account", "example").getFields();
            assertThat(fields, hasItem("field"));

            // And check patch
            Set<JsonNode> patchs = resource.get("app1", "v1", "account", "example").getPatchs();
            patchs.forEach(patch -> assertThat(patch.get("op").textValue(), is("add")));

        }

        @Test
        @Order(2)
        public void cleanAll() throws IOException {

            // Given build schema

            SchemaEntity resourceSchema = new SchemaEntity();
            resourceSchema.setApplication("app1");
            resourceSchema.setVersion("v1");
            resourceSchema.setResource("account");
            resourceSchema.setProfile("example");
            ;

            // When perform update

            resource.update(resourceSchema);

            // Then check schema
            JsonNode schemaResource = resource.get("app1", "v1", "account", "example").getContent();
            assertThat(schemaResource, is(SchemaResourceImpl.SCHEMA_DEFAULT));

            // And check filter
            Set<String> filter = resource.get("app1", "v1", "account", "example").getFilters();
            assertThat(filter, empty());

            // And check fields
            Set<String> fields = resource.get("app1", "v1", "account", "example").getFields();
            assertThat(fields, empty());

            // And check patch
            Set<JsonNode> patchs = resource.get("app1", "v1", "account", "example").getPatchs();
            assertThat(patchs, empty());

        }

    }

    @Test
    public void allVersions() {

        // Given create schema

        SchemaEntity resourceSchema1 = new SchemaEntity();
        resourceSchema1.setApplication("app2");
        resourceSchema1.setVersion("v1");
        resourceSchema1.setResource("product");
        resourceSchema1.setProfile("example1");

        resource.save(resourceSchema1);

        // And create other schema

        SchemaEntity resourceSchema2 = new SchemaEntity();
        resourceSchema2.setApplication("app2");
        resourceSchema2.setVersion("v2");
        resourceSchema2.setResource("product");
        resourceSchema2.setProfile("example2");

        resource.save(resourceSchema2);

        // When perform all versions
        Map<String, List<SchemaVersionProfileEntity>> versions = resource.allVersions("app2");

        // Then check result
        assertAll(
                () -> assertThat(versions.size(), is(1)),
                () -> assertThat(versions.get("product"), hasItem(allOf(new HasPropertyWithValue<SchemaVersionProfileEntity>("version", is("v1")),
                        new HasPropertyWithValue<SchemaVersionProfileEntity>("profile", is("example1"))))),
                () -> assertThat(versions.get("product"), hasItem(allOf(new HasPropertyWithValue<SchemaVersionProfileEntity>("version", is("v2")),
                        new HasPropertyWithValue<SchemaVersionProfileEntity>("profile", is("example2"))))));

    }

    @Test
    public void getEmptySchema() throws IOException {

        // When perform get
        JsonNode schemaResource = resource.get("app3", UUID.randomUUID().toString(), "account", "example").getContent();

        // Then check schema
        assertThat(schemaResource, is((SchemaResourceImpl.SCHEMA_DEFAULT)));

    }
}
