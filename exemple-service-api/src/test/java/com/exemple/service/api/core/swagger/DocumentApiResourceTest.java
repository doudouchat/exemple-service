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
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.resource.schema.SchemaResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DocumentApiResourceTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    public static final String URL = "/test/openapi.json";

    @Autowired
    private SchemaResource schemaResource;

    @BeforeMethod
    private void before() {

        Mockito.reset(schemaResource);

    }

    @Test
    public void swagger() throws Exception {

        Mockito.when(schemaResource.allVersions(Mockito.anyString())).thenReturn(Collections.singletonMap("account", Arrays.asList("v1", "v2")));

        Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        String baseUri = target(URL).getUri().toString().replace(URL, "");

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseBody = mapper.readTree(response.readEntity(String.class));

        assertThat(responseBody, notNullValue());
        assertThat(responseBody, hasJsonField("components", hasJsonField("schemas",
                // Account
                hasJsonField("Account.v1", hasJsonField("$ref", baseUri + "/ws/v1/schemas/account/test/v1")),
                // Account
                hasJsonField("Account.v2", hasJsonField("$ref", baseUri + "/ws/v1/schemas/account/test/v2")),
                // Stock
                hasJsonField("Stock", hasJsonField("type", "object"),
                        hasJsonField("properties", hasJsonField("increment", hasJsonField("type", "integer")))),
                // Patch
                hasJsonField("Patch", hasJsonField("$ref", baseUri + "/ws/v1/schemas/patch")),
                // Health
                hasJsonField("Health", hasJsonField("type", "object"),
                        hasJsonField("properties", hasJsonField("status", hasJsonField("type", "string"))))

        )));

    }

}
