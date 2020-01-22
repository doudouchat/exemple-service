package com.exemple.service.resource.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.schema.impl.SchemaResourceImpl;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class SchemaResourceTest extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaResource.class);

    @Autowired
    private SchemaResource resource;

    private byte[] schemaResource;

    @Test
    public void save() throws IOException {

        schemaResource = IOUtils.toByteArray(new ClassPathResource("test.json").getInputStream());

        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setApplication("app1");
        resourceSchema.setVersion("v1");
        resourceSchema.setResource("account");
        resourceSchema.setContent(schemaResource);
        resourceSchema.setFilters(Collections.singleton("filter"));
        resourceSchema.setRules(Collections.singletonMap("login", Collections.singleton("email")));

        resource.save(resourceSchema);

    }

    @Test(dependsOnMethods = "save")
    public void get() throws IOException {

        byte[] schemaResource = resource.get("app1", "v1", "account").getContent();

        assertThat(schemaResource, not(nullValue()));

        LOG.debug(IOUtils.toString(schemaResource, "utf-8"));

        assertThat(Arrays.equals(schemaResource, this.schemaResource), is(Boolean.TRUE));

    }

    @Test(dependsOnMethods = "get")
    public void update() throws IOException {

        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setApplication("app1");
        resourceSchema.setVersion("v1");
        resourceSchema.setResource("account");
        resourceSchema.setFilters(Collections.singleton("filter"));
        resourceSchema.setRules(Collections.singletonMap("login", Collections.singleton("email")));

        resource.update(resourceSchema);

        byte[] schemaResource = resource.get("app1", "v1", "account").getContent();

        assertThat(schemaResource, not(nullValue()));

        LOG.debug(IOUtils.toString(schemaResource, "utf-8"));

        assertThat(Arrays.equals(schemaResource, SchemaResourceImpl.SCHEMA_DEFAULT.getBytes()), is(Boolean.TRUE));

    }

    @Test(dependsOnMethods = "save")
    public void allVersions() {

        SchemaEntity resourceSchema1 = new SchemaEntity();
        resourceSchema1.setApplication("app1");
        resourceSchema1.setVersion("v1");
        resourceSchema1.setResource("product");

        resource.save(resourceSchema1);

        SchemaEntity resourceSchema2 = new SchemaEntity();
        resourceSchema2.setApplication("app1");
        resourceSchema2.setVersion("v2");
        resourceSchema2.setResource("product");

        resource.save(resourceSchema2);

        Map<String, List<String>> versions = resource.allVersions("app1");

        assertThat(versions.size(), is(2));
        assertThat(versions.get("account"), hasItem("v1"));
        assertThat(versions.get("product"), hasItem("v1"));
        assertThat(versions.get("product"), hasItem("v2"));
    }

    @Test
    public void getEmptySchema() throws IOException {
        String version = UUID.randomUUID().toString();

        SchemaEntity resourceSchema = new SchemaEntity();
        resourceSchema.setApplication("app2");
        resourceSchema.setVersion(version);
        resourceSchema.setResource("account");

        resource.save(resourceSchema);

        byte[] schemaResource = resource.get(resourceSchema.getApplication(), version, resourceSchema.getResource()).getContent();

        assertThat(schemaResource, not(nullValue()));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode schema = mapper.readTree(schemaResource);
        assertThat(schema, is(mapper.readTree(SchemaResourceImpl.SCHEMA_DEFAULT.getBytes())));

        schemaResource = resource.get(resourceSchema.getApplication(), UUID.randomUUID().toString(), resourceSchema.getResource()).getContent();
        schema = mapper.readTree(schemaResource);
        assertThat(schemaResource, not(nullValue()));
        assertThat(schema, is(mapper.readTree(SchemaResourceImpl.SCHEMA_DEFAULT.getBytes())));

    }

    @Test(dependsOnMethods = "save")
    public void getFilter() {

        Set<String> filter = resource.get("app1", "v1", "account").getFilters();

        assertThat(filter, hasItem("filter"));

    }

    @Test
    public void getFilterFailure() {

        Set<String> filter = resource.get("app1", UUID.randomUUID().toString(), "account").getFilters();

        assertThat(filter, empty());

    }

    @Test(dependsOnMethods = "save")
    public void getRule() {

        Map<String, Set<String>> filter = resource.get("app1", "v1", "account").getRules();

        assertThat(filter.get("login"), hasItem("email"));

    }

    @Test(dependsOnMethods = "save")
    public void getRuleEmpty() {

        Map<String, Set<String>> filter = resource.get("app1", UUID.randomUUID().toString(), "account").getRules();

        assertThat(filter.isEmpty(), is(true));

    }
}
