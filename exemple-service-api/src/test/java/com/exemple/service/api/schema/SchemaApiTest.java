package com.exemple.service.api.schema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;

public class SchemaApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private SchemaDescription service;

    @Autowired
    private JsonNode schema;

    @BeforeEach
    public void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/schemas";

    @Test
    public void get() {

        // Given mock service

        String resource = "account";
        String app = "default";
        String version = "v1";
        String profile = "user";

        Mockito.when(service.get(Mockito.eq(app), Mockito.eq(version), Mockito.eq(resource), Mockito.eq(profile))).thenReturn(schema);

        // When perform get

        Response response = target(URL + "/" + resource + "/" + app + "/" + version + "/" + profile).request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(schema));

    }

    @Test
    public void getPatch() {

        // Given mock service

        Mockito.when(service.getPatch()).thenReturn(schema);

        // When perform get

        Response response = target(URL + "/patch").request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(schema));

    }

    @Test
    public void getFailureNotFoundApplicationException() {

        // Given mock service

        String resource = "account";
        String app = "default";
        String version = "v1";
        String profile = "user";

        Mockito.when(service.get(Mockito.eq(app), Mockito.eq(version), Mockito.eq(resource), Mockito.eq(profile)))
                .thenThrow(new NotFoundApplicationException(app, null));

        // When perform get

        Response response = target(URL + "/" + resource + "/" + app + "/" + version + "/" + profile).request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

}
