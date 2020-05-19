package com.exemple.service.api.account;

import static com.exemple.service.api.common.security.ApiProfile.USER_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.schema.common.exception.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AccountApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @BeforeMethod
    private void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/accounts";

    @Test
    public void get() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenReturn(JsonNodeUtils.init());

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void getFailure() throws Exception {

        UUID id = UUID.randomUUID();

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "").header(SchemaBeanParam.VERSION_HEADER, "").get();

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

        JsonNode responseEntity = MAPPER.readTree(response.readEntity(InputStream.class));
        assertThat(responseEntity.has(SchemaBeanParam.APP_HEADER), is(true));
        assertThat(responseEntity.has(SchemaBeanParam.VERSION_HEADER), is(true));

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void update() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenReturn(JsonNodeUtils.create(Collections.singletonMap("lastname", null)));
        Mockito.when(
                service.save(Mockito.eq(id), Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenReturn(JsonNodeUtils.create(Collections.singletonMap("lastname", "Dupond")));

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

        Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(MAPPER.writeValueAsString(Collections.singletonList(patch))));

        Mockito.verify(service).save(Mockito.eq(id), Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"),
                Mockito.eq(USER_PROFILE.profile));
        Mockito.verify(service).get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

    }

    @Test
    public void updateFailure() throws Exception {

        UUID id = UUID.randomUUID();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "replace");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

        Mockito.when(service.get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenReturn(JsonNodeUtils.init());

        Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(MAPPER.writeValueAsString(Collections.singletonList(patch))));

        Mockito.verify(service).get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void create() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenReturn(JsonNodeUtils.create(Collections.singletonMap("id", id)));

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(JsonNodeUtils.init().toString()));

        Mockito.verify(service).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation(), is(URI.create(baseUri + "/" + id)));

    }

    @Test
    public void createFailure() throws Exception {

        SchemaBeanParam apiBeanParam = new SchemaBeanParam("", "");

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, apiBeanParam.getApp()).header(SchemaBeanParam.VERSION_HEADER, apiBeanParam.getVersion())
                .post(null);

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

        JsonNode responseEntity = MAPPER.readTree(response.readEntity(InputStream.class));
        assertThat(responseEntity.has(SchemaBeanParam.APP_HEADER), is(true));
        assertThat(responseEntity.has(SchemaBeanParam.VERSION_HEADER), is(true));

    }

    @DataProvider(name = "serviceFailures")
    private static Object[][] serviceFailures() {

        Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

        return new Object[][] {
                // constraint exception
                { new ConstraintViolationException(constraintViolations), Status.BAD_REQUEST },
                // validation schema exception
                { new ValidationException(), Status.BAD_REQUEST },
                // exception
                { new RuntimeException(), Status.INTERNAL_SERVER_ERROR },

                //
        };
    }

    @Test(dataProvider = "serviceFailures")
    public void createFailure(Exception exception, Status expectedStatus) throws Exception {

        Mockito.when(service.save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenThrow(exception);

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(JsonNodeUtils.init().toString()));

        Mockito.verify(service).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(expectedStatus.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void getNotFound() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenThrow(new AccountServiceNotFoundException());

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

}
