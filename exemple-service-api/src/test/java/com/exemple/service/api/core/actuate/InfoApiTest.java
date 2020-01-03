package com.exemple.service.api.core.actuate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class InfoApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    private static final String URL = "/";

    @Test
    public void template() throws Exception {

        Response response = target(URL).request().get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

    }

    @Test
    public void info() throws Exception {

        Response response = target(URL + "info").request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseBody = mapper.readTree(response.readEntity(String.class));

        assertThat(responseBody.get("version"), is(notNullValue()));
        assertThat(responseBody.get("buildTime"), is(notNullValue()));
    }

}
