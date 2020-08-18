package com.exemple.service.api.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
import com.fasterxml.jackson.databind.node.ArrayNode;

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

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(JsonNodeUtils.init());

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(id));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void update() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.save(Mockito.eq(id), Mockito.any(ArrayNode.class)))
                .thenReturn(JsonNodeUtils.create(Collections.singletonMap("lastname", "Dupond")));

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

        Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(MAPPER.writeValueAsString(Collections.singletonList(patch))));

        Mockito.verify(service).save(Mockito.eq(id), Mockito.any(ArrayNode.class));

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

    }

    @Test
    public void create() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.save(Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(Collections.singletonMap("id", id)));

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(JsonNodeUtils.init().toString()));

        Mockito.verify(service).save(Mockito.any(JsonNode.class));

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation(), is(URI.create(baseUri + "/" + id)));

    }

    @DataProvider(name = "serviceFailures")
    private static Object[][] serviceFailures() {

        Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();

        return new Object[][] {
                // constraint exception
                { new ConstraintViolationException(constraintViolations), Status.BAD_REQUEST },
                // validation schema exception
                { new ValidationException(new IllegalArgumentException()), Status.BAD_REQUEST },
                // exception
                { new RuntimeException(), Status.INTERNAL_SERVER_ERROR },

                //
        };
    }

    @Test(dataProvider = "serviceFailures")
    public void createFailure(Exception exception, Status expectedStatus) throws Exception {

        Mockito.when(service.save(Mockito.any(JsonNode.class))).thenThrow(exception);

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(JsonNodeUtils.init().toString()));

        Mockito.verify(service).save(Mockito.any(JsonNode.class));

        assertThat(response.getStatus(), is(expectedStatus.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void getNotFound() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(service.get(Mockito.eq(id))).thenThrow(new AccountServiceNotFoundException());

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(id));

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

}
