package com.exemple.service.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@SpringBootTest(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles({ "test", "AuthorizationMock" })
class AccountApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @Autowired
    private LoginService loginService;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private JsonNode account;

    @BeforeEach
    void before() {

        Mockito.reset(service, schemaValidation);

    }

    public static final String URL = "/v1/accounts";

    @Nested
    class get {

        @Test
        void success() {

            // Given account id

            var id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(account.deepCopy()));

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:read" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            var response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
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

            var id = UUID.randomUUID();

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:create" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            var response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
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

            var id = UUID.randomUUID();

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.empty());

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:read" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            var response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
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

            var id = UUID.randomUUID();

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.of(UUID.randomUUID()));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:read" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            var response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
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

            var id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(account.deepCopy()));

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:update" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform patch

            var patch = MAPPER.readTree(
                    """
                    [{"op": "add", "path": "/birthday", "value":"1976-12-12"}]
                    """);

            var response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .method("PATCH", Entity.json(patch));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

            // And check service

            var previousAccount = ArgumentCaptor.forClass(JsonNode.class);
            var actualAccount = ArgumentCaptor.forClass(JsonNode.class);

            var expectedAccount = ((ObjectNode) account.deepCopy()).put("birthday", "1976-12-12");

            Mockito.verify(service).update(actualAccount.capture());
            assertThat(actualAccount.getValue()).isEqualTo(expectedAccount);

            // And check validation

            var patchCapture = ArgumentCaptor.forClass(ArrayNode.class);

            Mockito.verify(schemaValidation).validate(Mockito.eq("account"), Mockito.eq("v1"), Mockito.anyString(),
                    patchCapture.capture(),
                    previousAccount.capture());
            assertAll(
                    () -> assertThat(previousAccount.getValue()).isEqualTo(account),
                    () -> assertThat(patchCapture.getValue()).isEqualTo(patch));

        }

        @Test
        void isForbidden() throws IOException {

            // Given account id

            var id = UUID.randomUUID();

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:read" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform patch

            var patch = MAPPER.readTree(
                    """
                    [{"op": "add", "path": "/birthday", "value":"1976-12-12"}]
                    """);

            var response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                    .request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .method("PATCH", Entity.json(patch));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).update(any());

        }

        @Test
        void JsonPatchException() throws IOException {

            // Given account id

            var id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(MAPPER.createObjectNode()));

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:update" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform patch

            var patch = MAPPER.readTree(
                    """
                    [{"op": "replace", "path": "/lastname", "value":"Dupond"}]
                    """);

            var response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
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

            var id = UUID.randomUUID();

            // And mock service

            Mockito.when(service.get(id)).thenReturn(Optional.of(account.deepCopy()));

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.of(id));

            // And mock login

            Mockito.when(loginService.get("john_doe")).thenReturn(Optional.of(id));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:update" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            var source = MAPPER.readTree(
                    """
                    {"lastname": "Dupond"}
                    """);

            var response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

            // And check service

            var sourceToUpdate = MAPPER.readTree(
                    """
                    {"lastname": "Dupond"}
                    """);

            var actualAccount = ArgumentCaptor.forClass(JsonNode.class);

            Mockito.verify(service).update(actualAccount.capture());
            assertThat(actualAccount.getValue()).isEqualTo(sourceToUpdate);

            // And check validation

            var sourceToValidate = MAPPER.readTree(
                    """
                    {"lastname": "Dupond"}
                    """);

            Mockito.verify(schemaValidation).validate(Mockito.eq("account"), Mockito.eq("v1"), Mockito.anyString(),
                    actualAccount.capture());
            assertThat(actualAccount.getValue()).isEqualTo(sourceToValidate);

        }

        @Test
        void isForbidden() throws IOException {

            // Given account id

            var id = UUID.randomUUID();

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:read" })
                    .subject("john_doe")
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            var source = MAPPER.readTree(
                    """
                    {"lastname": "Dupond"}
                    """);

            var response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).update(any());

        }

    }

    @Nested
    class create {

        @Test
        void success() throws IOException {

            // Given mock service

            Mockito.when(service.create(Mockito.any(JsonNode.class))).thenReturn(account.deepCopy());

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:create" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform post

            var source = MAPPER.readTree(
                    """
                    {"email": "jean.dupond@gmail.com", "lastname": "dupond", "firstname":"jean"}
                    """);

            var response = target(URL).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .post(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

            // And check location

            var baseUri = target(URL).getUri();
            assertThat(response.getLocation()).isEqualTo(URI.create(baseUri + "/" + account.get("id").textValue()));

            // And check service

            var actualAccount = ArgumentCaptor.forClass(JsonNode.class);
            Mockito.verify(service).create(actualAccount.capture());
            assertThat(actualAccount.getValue()).isEqualTo(source);

            // And check validation

            Mockito.verify(schemaValidation).validate(Mockito.eq("account"), Mockito.eq("v1"), Mockito.anyString(),
                    actualAccount.capture());
            assertThat(actualAccount.getValue()).isEqualTo(source);

        }

        @Test
        void isForbidden() throws IOException {

            // Given token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "account:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform post

            var source = MAPPER.readTree(
                    """
                    {"email": "jean.dupond@gmail.com", "lastname": "dupond", "firstname":"jean"}
                    """);

            var response = target(URL).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .post(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).create(any());

        }

    }

}
