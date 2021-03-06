package com.exemple.service.api.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.login.model.LoginEntity;
import com.fasterxml.jackson.databind.JsonNode;

public class LoginApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private LoginResource resource;

    @BeforeMethod
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

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

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

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

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

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

        // And check location

        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation(), is(URI.create(baseUri + "/" + username)));

        // And check service

        ArgumentCaptor<LoginEntity> login = ArgumentCaptor.forClass(LoginEntity.class);
        Mockito.verify(resource).save(login.capture());
        assertThat(login.getValue().getUsername(), is(username));
        assertThat(login.getValue().getId(), is(model.get("id")));

    }

    @Test
    public void createAlreadyExistFailure() throws UsernameAlreadyExistsException {

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

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

        // And check body

        JsonNode expectedMessage = JsonNodeUtils.create(() -> {

            Map<String, Object> message = new HashMap<>();
            message.put("path", "/username");
            message.put("code", "username");
            message.put("message", "[" + username + "] already exists");

            return Collections.singletonList(message);

        });
        assertThat(response.readEntity(JsonNode.class), is(expectedMessage));

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

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body
        @SuppressWarnings("unchecked")
        Map<String, Object> actualModel = response.readEntity(Map.class);
        assertThat(actualModel.get("username"), is(nullValue()));
        assertThat(actualModel.get("id"), is(entity.getId().toString()));

    }

    @Test
    public void getNotFound() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.empty());

        // When perform get

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

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

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        // And check mock

        Mockito.verify(resource).delete(Mockito.eq(username));

    }

}
