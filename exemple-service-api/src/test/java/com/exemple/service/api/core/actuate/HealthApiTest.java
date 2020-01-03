package com.exemple.service.api.core.actuate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HealthApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    public static final String URL = "/health";

    @Test
    public void health() throws Exception {

        Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseBody = mapper.readTree(response.readEntity(String.class));

        assertThat(responseBody.get("status").textValue(), is("UP"));
    }

}
