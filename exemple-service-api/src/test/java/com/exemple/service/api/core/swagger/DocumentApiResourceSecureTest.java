package com.exemple.service.api.core.swagger;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles(inheritProfiles = false)
public class DocumentApiResourceSecureTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    public static final String URL = "/test/openapi.json";

    @Test
    public void swagger() throws Exception {

        Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseBody = mapper.readTree(response.readEntity(String.class));

        assertThat(responseBody, notNullValue());
        assertThat(responseBody, hasJsonField("components",
                // shemas
                hasJsonField("schemas"),
                // security
                hasJsonField("securitySchemes",
                        // oauth2 password
                        hasJsonField(DocumentApiResource.OAUTH2_PASS, hasJsonField("type", "oauth2")),
                        // oauth2 client credentials
                        hasJsonField(DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS, hasJsonField("type", "oauth2")),
                        // bearer
                        hasJsonField(DocumentApiResource.BEARER_AUTH, hasJsonField("type", "http")))));

    }

}
