package com.exemple.service.api.core.swagger;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;
import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocumentApiResourceTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    public static final String URL = "/test/openapi.json";

    private static final String ACCOUNT_V1_URL = "/ws/v1/schemas/account/test/v1/user";

    private static final String ACCOUNT_V2_URL = "/ws/v1/schemas/account/test/v2/admin";

    private static final String PATCH_URL = "/ws/v1/schemas/patch";

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private SchemaDescription schemaDescription;

    @BeforeMethod
    private void before() {

        Mockito.reset(schemaResource);

    }

    @Test
    public void swagger() throws Exception {

        Mockito.when(schemaResource.allVersions(Mockito.anyString())).thenReturn(Collections.singletonMap("account",
                Arrays.asList(new SchemaVersionProfileEntity("v1", "user"), new SchemaVersionProfileEntity("v2", "admin"))));

        Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseBody = mapper.readTree(response.readEntity(String.class));

        assertThat(responseBody, notNullValue());
        assertThat(responseBody, hasJsonField("components", hasJsonField("schemas",
                // Account
                hasJsonField("Account.v1.user", hasJsonField("$ref", ACCOUNT_V1_URL)),
                // Account
                hasJsonField("Account.v2.admin", hasJsonField("$ref", ACCOUNT_V2_URL)),
                // Stock
                hasJsonField("Stock", hasJsonField("type", "object"),
                        hasJsonField("properties", hasJsonField("increment", hasJsonField("type", "integer")))),
                // Patch
                hasJsonField("Patch", hasJsonField("$ref", PATCH_URL))

        )));

    }

    @DataProvider(name = "schemas")
    private static Object[][] schemas() {

        return new Object[][] { { ACCOUNT_V1_URL }, { ACCOUNT_V2_URL } };
    }

    @Test(dataProvider = "schemas", dependsOnMethods = "swagger")
    public void schemas(String url) throws Exception {

        Mockito.when(schemaDescription.get(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(JsonNodeUtils.init());

        Response response = target(url.replaceFirst("ws/", "")).request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test(dependsOnMethods = "swagger")
    public void patch() {

        Mockito.when(schemaDescription.getPatch()).thenReturn(JsonNodeUtils.init());

        Response response = target(PATCH_URL.replaceFirst("ws/", "")).request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

}
