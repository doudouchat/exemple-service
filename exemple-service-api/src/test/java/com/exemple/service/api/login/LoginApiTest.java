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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LoginApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private LoginService service;

    @BeforeMethod
    public void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/logins";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void check() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.when(service.exist(Mockito.eq(login))).thenReturn(true);

        Response response = target(URL + "/" + login).request().header(APP_HEADER, "test").head();

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        Mockito.verify(service).exist(login);

    }

    @Test
    public void checkNotFound() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.when(service.exist(Mockito.eq(login))).thenReturn(false);

        Response response = target(URL + "/" + login).request().header(APP_HEADER, "test").head();

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

        Mockito.verify(service).exist(login);

    }

    @Test
    public void create() throws Exception {

        String login = "jean.dupond@gmail.com";

        Map<String, Object> model = new HashMap<>();
        model.put("username", login);
        model.put("password", "jean.dupont");
        model.put("id", UUID.randomUUID());

        Mockito.doNothing().when(service).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(JsonNodeUtils.create(model)));

        Mockito.verify(service).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation(), is(URI.create(baseUri + "/" + login)));

    }

    @Test
    public void update() throws Exception {

        String login = "jean.dupond@gmail.com";

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        model.put("id", UUID.randomUUID());

        Mockito.when(service.get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.create(model));

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", "mdp");

        Response response = target(URL + "/" + login).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(MAPPER.writeValueAsString(Collections.singletonList(patch))));

        Mockito.verify(service).save(Mockito.eq(login), Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));
        Mockito.verify(service).get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

    }

    @Test
    public void updateFailure() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.when(service.get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"))).thenThrow(new LoginServiceNotFoundException());

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", "mdp");

        Response response = target(URL + "/" + login).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(MAPPER.writeValueAsString(Collections.singletonList(patch))));

        Mockito.verify(service).get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"));
        Mockito.verify(service, Mockito.never()).save(Mockito.eq(login), Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

    @Test
    public void get() throws Exception {

        String login = "jean.dupond@gmail.com";

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        model.put("id", UUID.randomUUID());

        Mockito.when(service.get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.create(model));

        Response response = target(URL + "/" + login).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

    }

    @Test
    public void getFailure() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.when(service.get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"))).thenThrow(new LoginServiceNotFoundException());

        Response response = target(URL + "/" + login).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(login), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

    @Test
    public void delete() throws Exception {

        String login = "jean.dupond@gmail.com";

        Mockito.doNothing().when(service).delete(Mockito.eq(login));

        Response response = target(URL + "/" + login).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .delete();

        Mockito.verify(service).delete(Mockito.eq(login));

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

    }

}
