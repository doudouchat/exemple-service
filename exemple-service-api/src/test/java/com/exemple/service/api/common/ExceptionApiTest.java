package com.exemple.service.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.account.AccountApiTest;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.api.stock.StockApiTest;
import com.exemple.service.customer.account.AccountService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ExceptionApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @BeforeEach
    private void before() {

        Mockito.reset(service);

    }

    @Test
    public void notFound() {

        // When perform get

        Response response = target("/v1/notfound").request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    }

    @Test
    public void notAcceptable() {

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).request(MediaType.TEXT_HTML).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_ACCEPTABLE.getStatusCode());

    }

    @Test
    public void JsonException() {

        // When perform post

        Response response = target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON).post(Entity.json("toto"));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void JsonEmptyException() {

        // When perform past

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(Collections.EMPTY_LIST));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void JsonPatchException() throws IOException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(Optional.of(MAPPER.createObjectNode()));

        // When perform patch

        JsonNode patch = MAPPER.readTree("{\"op\": \"replace\", \"path\": \"/lastname\", \"value\":\"Dupond\"}");

        Response response = target(AccountApiTest.URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(Collections.singletonList(patch)));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

    }

    @Test
    public void unrecognizedPropertyException() {

        // When perform post

        Response response = target(StockApiTest.URL + "/store/product").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json("{\"other\":10, \"increment\":5}"));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("One or more fields are unrecognized");

    }

    @Test
    public void internalServerError() {

        // Given mock service

        Mockito.when(service.get(Mockito.any(UUID.class))).thenThrow(new RuntimeException());

        // When perform post

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

}
