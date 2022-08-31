package com.exemple.service.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.net.URI;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

class AccountApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private JsonNode account;

    @BeforeEach
    private void before() {

        Mockito.reset(service, schemaValidation);

    }

    public static final String URL = "/v1/accounts";

    @Test
    void get() {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(id)).thenReturn(Optional.of(account));

        // When perform get

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check body

        assertThat(response.readEntity(JsonNode.class)).isEqualTo(account);

    }

    @Test
    void patch() throws IOException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(id)).thenReturn(Optional.of(account));

        // When perform patch

        JsonNode patch = MAPPER.readTree(
                """
                [{"op": "add", "path": "/birthday", "value":"1976-12-12"}]
                """);

        Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(patch));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

        // And check service

        ArgumentCaptor<JsonNode> previousAccount = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);

        JsonNode expectedAccount = ((ObjectNode) this.account).put("birthday", "1976-12-12");

        Mockito.verify(service).save(account.capture(), previousAccount.capture());
        assertAll(
                () -> assertThat(previousAccount.getValue()).isEqualTo(this.account),
                () -> assertThat(account.getValue()).isEqualTo(expectedAccount));

        // And check validation

        ArgumentCaptor<ArrayNode> patchCapture = ArgumentCaptor.forClass(ArrayNode.class);

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"),
                patchCapture.capture(),
                previousAccount.capture());
        assertAll(
                () -> assertThat(previousAccount.getValue()).isEqualTo(this.account),
                () -> assertThat(patchCapture.getValue()).isEqualTo(patch));

    }

    @Test
    void put() throws IOException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service
        Mockito.when(service.get(id)).thenReturn(Optional.of(this.account));

        // When perform put

        JsonNode source = MAPPER.readTree(
                """
                {"lastname": "Dupond"}
                """);

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .put(Entity.json(source));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

        // And check service

        JsonNode sourceToValidate = MAPPER.readTree(
                """
                {"id": "%s", "lastname": "Dupond"}
                """.formatted(id));

        ArgumentCaptor<JsonNode> previousAccount = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(service).save(account.capture(), previousAccount.capture());
        assertAll(
                () -> assertThat(previousAccount.getValue()).isEqualTo(this.account),
                () -> assertThat(account.getValue()).isEqualTo(sourceToValidate));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"), account.capture(),
                previousAccount.capture());
        assertAll(
                () -> assertThat(previousAccount.getValue()).isEqualTo(this.account),
                () -> assertThat(account.getValue()).isEqualTo(sourceToValidate));

    }

    @Test
    void create() throws IOException {

        // Given mock service

        Mockito.when(service.save(Mockito.any(JsonNode.class))).thenReturn(this.account);

        // When perform post

        JsonNode source = MAPPER.readTree(
                """
                {"email": "jean.dupond@gmail.com", "lastname": "dupond", "firstname":"jean"}
                """);

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(source));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

        // And check location

        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation()).isEqualTo(URI.create(baseUri + "/" + this.account.get("id").textValue()));

        // And check service

        ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(service).save(account.capture());
        assertThat(account.getValue()).isEqualTo(source);

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"),
                account.capture());
        assertThat(account.getValue()).isEqualTo(source);

    }

}
