package com.exemple.service.api.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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

import com.exemple.service.api.account.AccountApiTest;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.api.stock.StockApiTest;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;

public class ExceptionApiTest extends JerseySpringSupport {

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

    @Test
    public void notFound() {

        // When perform get

        Response response = target("/v1/notfound").request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

    @Test
    public void notAcceptable() {

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).request(MediaType.TEXT_HTML).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_ACCEPTABLE.getStatusCode()));

    }

    @Test
    public void JsonException() {

        // When perform post

        Response response = target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON).post(Entity.json("toto"));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void JsonEmptyException() {

        // When perform past

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(Collections.EMPTY_LIST));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
    }

    @Test
    public void JsonPatchException() throws AccountServiceNotFoundException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(JsonNodeUtils.create(() -> "{}"));

        // When perform patch

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "replace");
        patch.put("path", "/lastname");
        patch.put("value", "Dupond");

        Response response = target(AccountApiTest.URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(JsonNodeUtils.toString(Collections.singletonList(patch))));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

    }

    @Test
    public void unrecognizedPropertyException() {

        // When perform post

        Response response = target(StockApiTest.URL + "/store/product").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json("{\"other\":10, \"increment\":5}"));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

        // And check body

        assertThat(response.readEntity(String.class), is("One or more fields are unrecognized"));

    }

    @Test
    public void internalServerError() throws AccountServiceException {

        // Given mock service

        Mockito.when(service.get(Mockito.any(UUID.class))).thenThrow(new RuntimeException());

        // When perform post

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").get();

        // Then check status

        assertThat(response.getStatus(), is(Status.INTERNAL_SERVER_ERROR.getStatusCode()));

    }

}
