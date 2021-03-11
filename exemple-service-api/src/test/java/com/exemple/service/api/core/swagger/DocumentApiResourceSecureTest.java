package com.exemple.service.api.core.swagger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupportSecure;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

public class DocumentApiResourceSecureTest extends JerseySpringSupportSecure {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    public static final String URL = "/test/openapi.json";

    @Autowired
    @Qualifier("swagger_security")
    private JsonNode swaggerSecurity;

    @Test
    public void swagger() {

        // When perform get

        Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        JsonNode swagger = response.readEntity(JsonNode.class);
        assertThat(swagger.at("/components/securitySchemes"), is(swaggerSecurity));

    }

}
