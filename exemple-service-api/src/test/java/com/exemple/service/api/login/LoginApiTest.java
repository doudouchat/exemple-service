package com.exemple.service.api.login;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.login.LoginResource;

class LoginApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private LoginResource resource;

    @BeforeEach
    void before() {

        Mockito.reset(resource);

    }

    public static final String URL = "/v1/logins";

    @Test
    void check() {

        // Given login

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(resource.get(username)).thenReturn(Optional.of(UUID.randomUUID()));

        // When perform head

        Response response = target(URL + "/" + username).request().header(SchemaBeanParam.APP_HEADER, "test").head();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    }

    @Test
    void checkNotFound() throws Exception {

        // Given username

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(resource.get(username)).thenReturn(Optional.empty());

        // When perform head

        Response response = target(URL + "/" + username).request().header(SchemaBeanParam.APP_HEADER, "test").head();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    }

    @Test
    void get() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(username)).thenReturn(Optional.of(id));

        // When perform get

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // And check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check body
        assertThat(response.readEntity(UUID.class)).isEqualTo(id);

    }

}
