package com.exemple.service.api.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.login.model.LoginEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private LoginResource resource;

    @BeforeEach
    public void before() {

        Mockito.reset(resource);

    }

    public static final String URL = "/v1/logins";

    @Test
    public void check() {

        // Given login

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.of(new LoginEntity()));

        // When perform head

        Response response = target(URL + "/" + username).request().header(SchemaBeanParam.APP_HEADER, "test").head();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

    }

    @Test
    public void checkNotFound() throws Exception {

        // Given username

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.empty());

        // When perform head

        Response response = target(URL + "/" + username).request().header(SchemaBeanParam.APP_HEADER, "test").head();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    }

    @Test
    public void create() throws UsernameAlreadyExistsException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // When perform post

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("username", username);
        model.put("id", UUID.randomUUID());

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test")

                .post(Entity.json(model));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

        // And check location

        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation()).isEqualTo(URI.create(baseUri + "/" + username));

        // And check service

        ArgumentCaptor<LoginEntity> login = ArgumentCaptor.forClass(LoginEntity.class);
        Mockito.verify(resource).save(login.capture());
        assertAll(
                () -> assertThat(login.getValue().getUsername()).isEqualTo(username),
                () -> assertThat(login.getValue().getId()).isEqualTo(model.get("id")));

    }

    @Test
    public void createAlreadyExistFailure() throws UsernameAlreadyExistsException, IOException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.doThrow(new UsernameAlreadyExistsException(username)).when(resource).save(Mockito.any());

        // When perform post

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("username", username);
        model.put("id", UUID.randomUUID());

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(model));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        JsonNode expectedMessage = MAPPER
                .readTree("[{\"path\": \"/username\", \"code\": \"username\", \"message\":\"[" + username + "] already exists\"}]");

        assertThat(response.readEntity(JsonNode.class)).isEqualTo(expectedMessage);

    }

    @Test
    public void get() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        LoginEntity entity = new LoginEntity();
        entity.setUsername(username);
        entity.setId(UUID.randomUUID());

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.of(entity));

        // When perform get

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // And check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check body
        @SuppressWarnings("unchecked")
        Map<String, Object> actualModel = response.readEntity(Map.class);
        assertAll(
                () -> assertThat(actualModel.get("username")).isNull(),
                () -> assertThat(actualModel.get("id")).isEqualTo(entity.getId().toString()));

    }

    @Test
    public void delete() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // When perform delete

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .delete();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

        // And check mock

        Mockito.verify(resource).delete(Mockito.eq(username));

    }

}
