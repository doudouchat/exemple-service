package com.exemple.service.resource.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.hamcrest.beans.HasPropertyWithValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.schema.impl.SchemaResourceImpl;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class SchemaResourceTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaResource.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource resource;

    private JsonNode schemaResource;

    @BeforeMethod
    public void resetResourceContext() {

        ResourceExecutionContext.destroy();

    }

    @Test
    public void save() throws IOException {

        schemaResource = MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("test.json").getInputStream()));

        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setApplication("app1");
        resourceSchema.setVersion("v1");
        resourceSchema.setResource("account");
        resourceSchema.setProfile("example");
        resourceSchema.setContent(schemaResource);
        resourceSchema.setFilters(Collections.singleton("filter"));
        resourceSchema.setFields((Collections.singleton("field")));
        resourceSchema.setPatchs(Collections.singleton(JsonNodeUtils.create(Collections.singletonMap("op", "add"))));

        resource.save(resourceSchema);

    }

    @Test(dependsOnMethods = "save")
    public void get() throws IOException {

        JsonNode schemaResource = resource.get("app1", "v1", "account", "example").getContent();

        assertThat(schemaResource, not(nullValue()));

        LOG.debug(schemaResource.toPrettyString());

        assertThat(schemaResource, is(this.schemaResource));

    }

    @Test(dependsOnMethods = "get")
    public void update() throws IOException {

        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setApplication("app1");
        resourceSchema.setVersion("v1");
        resourceSchema.setResource("account");
        resourceSchema.setProfile("example");
        resourceSchema.setFilters(Collections.singleton("filter"));
        resourceSchema.setPatchs(Collections.singleton(JsonNodeUtils.init()));

        resource.update(resourceSchema);

        JsonNode schemaResource = resource.get("app1", "v1", "account", "example").getContent();

        assertThat(schemaResource, not(nullValue()));

        LOG.debug(schemaResource.toPrettyString());

        assertThat(schemaResource, is(SchemaResourceImpl.SCHEMA_DEFAULT));

    }

    @Test(dependsOnMethods = "save")
    public void allVersions() {

        SchemaEntity resourceSchema1 = new SchemaEntity();
        resourceSchema1.setApplication("app1");
        resourceSchema1.setVersion("v1");
        resourceSchema1.setResource("product");
        resourceSchema1.setProfile("example1");

        resource.save(resourceSchema1);

        SchemaEntity resourceSchema2 = new SchemaEntity();
        resourceSchema2.setApplication("app1");
        resourceSchema2.setVersion("v2");
        resourceSchema2.setResource("product");
        resourceSchema2.setProfile("example2");

        resource.save(resourceSchema2);

        Map<String, List<SchemaVersionProfileEntity>> versions = resource.allVersions("app1");

        assertThat(versions.size(), is(2));
        assertThat(versions.get("account"), hasItem(allOf(new HasPropertyWithValue<SchemaVersionProfileEntity>("version", is("v1")),
                new HasPropertyWithValue<SchemaVersionProfileEntity>("profile", is("example")))));
        assertThat(versions.get("product"), hasItem(allOf(new HasPropertyWithValue<SchemaVersionProfileEntity>("version", is("v1")),
                new HasPropertyWithValue<SchemaVersionProfileEntity>("profile", is("example1")))));
        assertThat(versions.get("product"), hasItem(allOf(new HasPropertyWithValue<SchemaVersionProfileEntity>("version", is("v2")),
                new HasPropertyWithValue<SchemaVersionProfileEntity>("profile", is("example2")))));

    }

    @Test
    public void getEmptySchema() throws IOException {
        String version = UUID.randomUUID().toString();

        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setApplication("app2");
        resourceSchema.setVersion(version);
        resourceSchema.setResource("account");
        resourceSchema.setProfile("example");

        resource.save(resourceSchema);

        JsonNode schemaResource = resource.get(resourceSchema.getApplication(), version, resourceSchema.getResource(), resourceSchema.getProfile())
                .getContent();

        assertThat(schemaResource, not(nullValue()));
        assertThat(schemaResource, is((SchemaResourceImpl.SCHEMA_DEFAULT)));

        schemaResource = resource
                .get(resourceSchema.getApplication(), UUID.randomUUID().toString(), resourceSchema.getResource(), resourceSchema.getProfile())
                .getContent();
        assertThat(schemaResource, not(nullValue()));
        assertThat(schemaResource, is(SchemaResourceImpl.SCHEMA_DEFAULT));

    }

    @Test(dependsOnMethods = "save")
    public void getFilter() {

        Set<String> filter = resource.get("app1", "v1", "account", "example").getFilters();

        assertThat(filter, hasItem("filter"));

    }

    @Test
    public void getFilterFailure() {

        Set<String> filter = resource.get("app1", UUID.randomUUID().toString(), "account", "example").getFilters();

        assertThat(filter, empty());

    }

    @Test(dependsOnMethods = "save")
    public void getFields() {

        Set<String> fields = resource.get("app1", "v1", "account", "example").getFields();

        assertThat(fields, hasItem("field"));

    }

    @Test(dependsOnMethods = "save")
    public void getPatch() {

        Set<JsonNode> patchs = resource.get("app1", "v1", "account", "example").getPatchs();

        patchs.forEach(patch -> assertThat(patch.get("op").textValue(), is("add")));

    }

    @Test(dependsOnMethods = "save")
    public void getPatchEmpty() {

        Set<JsonNode> patchs = resource.get("app1", UUID.randomUUID().toString(), "account", "example").getPatchs();

        assertThat(patchs.isEmpty(), is(true));

    }
}
