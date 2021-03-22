package com.exemple.service.api.login;

import static com.exemple.service.api.common.model.ApplicationBeanParam.APP_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
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
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.schema.merge.SchemaMerge;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;

public class LoginApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private LoginService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private SchemaMerge schemaMerge;

    @Autowired
    private JsonNode login;

    @BeforeMethod
    public void before() {

        Mockito.reset(service, schemaValidation, schemaMerge);

    }

    public static final String URL = "/v1/logins";

    @Test
    public void check() {

        // Given login

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.exist(Mockito.eq(username))).thenReturn(true);

        // When perform head

        Response response = target(URL + "/" + username).request().header(APP_HEADER, "test").head();

        // Then check status

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

    }

    @Test
    public void checkNotFound() throws Exception {

        // Given login

        String login = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.exist(Mockito.eq(login))).thenReturn(false);

        // When perform head

        Response response = target(URL + "/" + login).request().header(APP_HEADER, "test").head();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

    @Test
    public void create() throws LoginServiceAlreadyExistException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // When perform post

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "jean.dupont");

            return model;

        });

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(source));

        // Then check status

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

        // And check location

        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation(), is(URI.create(baseUri + "/" + username)));

        // And check service

        ArgumentCaptor<JsonNode> login = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(service).save(login.capture());
        assertThat(login.getValue(), is(source));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("login"), login.capture());
        assertThat(login.getValue(), is(source));

    }

    @Test
    public void createAlreadyExistFailure() throws LoginServiceAlreadyExistException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.doThrow(new LoginServiceAlreadyExistException(username, null)).when(service).save(Mockito.any(JsonNode.class));

        // When perform post

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "jean.dupont");

            return model;

        });

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(source));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

        // And check body

        JsonNode expectedMessage = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("path", "/username");
            model.put("code", "username");
            model.put("message", "[" + username + "] already exists");

            return Collections.singletonList(model);

        });
        assertThat(response.readEntity(JsonNode.class), is(expectedMessage));

    }

    @Test
    public void update() throws LoginServiceNotFoundException, LoginServiceAlreadyExistException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(username))).thenReturn(this.login);

        // When perform past

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/enabled");
        patch.put("value", true);

        Response response = target(URL + "/" + username).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(JsonNodeUtils.toString(Collections.singletonList(patch))));

        // Then check status

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        // And check service

        ArgumentCaptor<JsonNode> previousLogin = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> login = ArgumentCaptor.forClass(JsonNode.class);

        JsonNode expectedLogin = JsonNodeUtils.set(this.login, "enabled", BooleanNode.TRUE);

        Mockito.verify(service).save(Mockito.eq(username), login.capture(), previousLogin.capture());
        assertThat(previousLogin.getValue(), is(this.login));
        assertThat(login.getValue(), is(expectedLogin));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("login"), login.capture(),
                previousLogin.capture());
        assertThat(previousLogin.getValue(), is(this.login));
        assertThat(login.getValue(), is(expectedLogin));

    }

    @Test
    public void put() throws LoginServiceNotFoundException, LoginServiceAlreadyExistException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(username))).thenReturn(this.login);

        // When perform past

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("password", "mdp");

            return model;

        });

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .put(Entity.json(source));

        // Then check status

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        // And check service

        ArgumentCaptor<JsonNode> previousLogin = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> login = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(service).save(Mockito.eq(username), login.capture(), previousLogin.capture());
        assertThat(previousLogin.getValue(), is(this.login));
        assertThat(login.getValue(), is(source));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("login"), login.capture(),
                previousLogin.capture());
        assertThat(previousLogin.getValue(), is(this.login));
        assertThat(login.getValue(), is(source));

        // And check merge
        Mockito.verify(schemaMerge).mergeMissingFieldFromOriginal(Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq("login"), Mockito.anyString(),
                login.capture(), previousLogin.capture());
        assertThat(previousLogin.getValue(), is(this.login));
        assertThat(login.getValue(), is(source));

    }

    @Test
    public void get() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(username))).thenReturn(login);

        // When perform get

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // And check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(login));

    }

    @Test
    public void getNotFound() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(username))).thenThrow(new LoginServiceNotFoundException());

        // When perform get

        Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

    @Test
    public void getById() throws LoginServiceNotFoundException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(Collections.singletonList(login));

        // When perform get

        Response response = target(URL + "/id/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(JsonNodeUtils.create(() -> Collections.singletonList(login))));

    }

    @Test
    public void getByIdNotFound() throws LoginServiceNotFoundException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(Mockito.eq(id))).thenThrow(new LoginServiceNotFoundException());

        // When perform get

        Response response = target(URL + "/id/" + id).request(MediaType.APPLICATION_JSON)

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

        Mockito.verify(service).delete(Mockito.eq(username));

    }

}
