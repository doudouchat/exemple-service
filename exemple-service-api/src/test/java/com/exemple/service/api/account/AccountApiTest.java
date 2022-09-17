package com.exemple.service.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginResource;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringJUnitConfig(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles("AuthorizationMock")
class AccountApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private JsonNode account;

    @BeforeEach
    private void before() {

        Mockito.reset(service, schemaValidation);

    }

    public static final String URL = "/v1/accounts";

    @Nested
    class get {

        @Test
        void success() {

            // Given account id

            UUID id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(account));

            // And mock login

            Mockito.when(loginResource.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:read" }).sign(Algorithm.none());

            // When perform get

            Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

            // And check body

            assertThat(response.readEntity(JsonNode.class)).isEqualTo(account);

        }

        @Test
        void isForbidden() {

            // Given account id

            UUID id = UUID.randomUUID();

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:create" }).sign(Algorithm.none());

            // When perform get

            Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).get(any());

        }

        @Test
        void tokenFromUnknownAccount() {

            // Given account id

            UUID id = UUID.randomUUID();

            // And mock login

            Mockito.when(loginResource.get("john_doe")).thenReturn(Optional.empty());

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:read" }).sign(Algorithm.none());

            // When perform get

            Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).get(any());

        }

        @Test
        void tokenFromAnotherAccount() {

            // Given account id

            UUID id = UUID.randomUUID();

            // And mock login

            Mockito.when(loginResource.get("john_doe")).thenReturn(Optional.of(UUID.randomUUID()));

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:read" }).sign(Algorithm.none());

            // When perform get

            Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).get(any());

        }

    }

    @Nested
    class patch {

        @Test
        void success() throws IOException {

            // Given account id

            UUID id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(account));

            // And mock login

            Mockito.when(loginResource.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:update" }).sign(Algorithm.none());

            // When perform patch

            JsonNode patch = MAPPER.readTree(
                    """
                    [{"op": "add", "path": "/birthday", "value":"1976-12-12"}]
                    """);

            Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .method("PATCH", Entity.json(patch));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

            // And check service

            ArgumentCaptor<JsonNode> previousAccount = ArgumentCaptor.forClass(JsonNode.class);
            ArgumentCaptor<JsonNode> actualAccount = ArgumentCaptor.forClass(JsonNode.class);

            JsonNode expectedAccount = ((ObjectNode) account).put("birthday", "1976-12-12");

            Mockito.verify(service).save(actualAccount.capture(), previousAccount.capture());
            assertAll(
                    () -> assertThat(previousAccount.getValue()).isEqualTo(account),
                    () -> assertThat(actualAccount.getValue()).isEqualTo(expectedAccount));

            // And check validation

            ArgumentCaptor<ArrayNode> patchCapture = ArgumentCaptor.forClass(ArrayNode.class);

            Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"),
                    patchCapture.capture(),
                    previousAccount.capture());
            assertAll(
                    () -> assertThat(previousAccount.getValue()).isEqualTo(account),
                    () -> assertThat(patchCapture.getValue()).isEqualTo(patch));

        }

        @Test
        void isForbidden() throws IOException {

            // Given account id

            UUID id = UUID.randomUUID();

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:read" }).sign(Algorithm.none());

            // When perform patch

            JsonNode patch = MAPPER.readTree(
                    """
                    [{"op": "add", "path": "/birthday", "value":"1976-12-12"}]
                    """);

            Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .method("PATCH", Entity.json(patch));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).save(any(), any());

        }

        @Test
        void JsonPatchException() throws IOException {

            // Given account id

            UUID id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(MAPPER.createObjectNode()));

            // And mock login

            Mockito.when(loginResource.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:update" }).sign(Algorithm.none());

            // When perform patch

            JsonNode patch = MAPPER.readTree(
                    """
                    [{"op": "replace", "path": "/lastname", "value":"Dupond"}]
                    """);

            Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .method("PATCH", Entity.json(patch));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

            // And check body

            assertThat(response.readEntity(String.class)).isEqualTo("Missing field \"lastname\"");

        }

    }

    @Nested
    class put {

        @Test
        void success() throws IOException {

            // Given account id

            UUID id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(account));

            // And mock login

            Mockito.when(loginResource.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:update" }).sign(Algorithm.none());

            // When perform put

            JsonNode source = MAPPER.readTree(
                    """
                    {"lastname": "Dupond"}
                    """);

            Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

            // And check service

            JsonNode sourceToValidate = MAPPER.readTree(
                    """
                    {"id": "%s", "lastname": "Dupond"}
                    """.formatted(id));

            ArgumentCaptor<JsonNode> previousAccount = ArgumentCaptor.forClass(JsonNode.class);
            ArgumentCaptor<JsonNode> actualAccount = ArgumentCaptor.forClass(JsonNode.class);

            Mockito.verify(service).save(actualAccount.capture(), previousAccount.capture());
            assertAll(
                    () -> assertThat(previousAccount.getValue()).isEqualTo(account),
                    () -> assertThat(actualAccount.getValue()).isEqualTo(sourceToValidate));

            // And check validation

            Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"),
                    actualAccount.capture(),
                    previousAccount.capture());
            assertAll(
                    () -> assertThat(previousAccount.getValue()).isEqualTo(account),
                    () -> assertThat(actualAccount.getValue()).isEqualTo(sourceToValidate));

        }

        @Test
        void isForbidden() throws IOException {

            // Given account id

            UUID id = UUID.randomUUID();

            // and token

            String token = JWT.create().withSubject("john_doe").withArrayClaim("scope", new String[] { "account:read" }).sign(Algorithm.none());

            // When perform put

            JsonNode source = MAPPER.readTree(
                    """
                    {"lastname": "Dupond"}
                    """);

            Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).save(any(), any());

        }

    }

    @Nested
    class create {

        @Test
        void success() throws IOException {

            // Given mock service

            Mockito.when(service.save(Mockito.any(JsonNode.class))).thenReturn(account);

            // and token

            String token = JWT.create().withArrayClaim("scope", new String[] { "account:create" }).sign(Algorithm.none());

            // When perform post

            JsonNode source = MAPPER.readTree(
                    """
                    {"email": "jean.dupond@gmail.com", "lastname": "dupond", "firstname":"jean"}
                    """);

            Response response = target(URL).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .post(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

            // And check location

            URI baseUri = target(URL).getUri();
            assertThat(response.getLocation()).isEqualTo(URI.create(baseUri + "/" + account.get("id").textValue()));

            // And check service

            ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);
            Mockito.verify(service).save(account.capture());
            assertThat(account.getValue()).isEqualTo(source);

            // And check validation

            Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"),
                    account.capture());
            assertThat(account.getValue()).isEqualTo(source);

        }

        @Test
        void isForbidden() throws IOException {

            // Given token

            String token = JWT.create().withArrayClaim("scope", new String[] { "account:read" }).sign(Algorithm.none());

            // When perform post

            JsonNode source = MAPPER.readTree(
                    """
                    {"email": "jean.dupond@gmail.com", "lastname": "dupond", "firstname":"jean"}
                    """);

            Response response = target(URL).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .post(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).save(any());

        }

    }

}
