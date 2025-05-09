package com.exemple.service.api.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.util.Optional;

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
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;

@SpringBootTest(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles({ "test", "AuthorizationMock" })
class SubscriptionApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private SubscriptionService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private JsonNode subscription;

    @BeforeEach
    void before() {

        Mockito.reset(service, schemaValidation);

    }

    public static final String URL = "/v1/subscriptions";

    @Nested
    class save {

        @Test
        void success() throws IOException {

            // Given email

            var email = "jean.dupond@gmail.com";

            // And mock service
            Mockito.when(service.get(email)).thenReturn(Optional.empty());

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "subscription:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            var source = MAPPER.readTree(
                    """
                    {"lastname": "dupond", "firstname":"jean"}
                    """);

            var response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

            // And check service

            var actualSubscription = ArgumentCaptor.forClass(JsonNode.class);

            Mockito.verify(service).create(Mockito.eq(email), actualSubscription.capture());
            assertThat(actualSubscription.getValue()).isEqualTo(source);

            // And check validation

            var sourceToValidate = MAPPER.readTree(
                    """
                    {"email": "%s", "lastname": "dupond", "firstname":"jean"}
                    """.formatted(email));

            Mockito.verify(schemaValidation).validate(Mockito.eq("subscription"), Mockito.eq("v1"), Mockito.anyString(),
                    actualSubscription.capture());
            assertThat(actualSubscription.getValue()).isEqualTo(sourceToValidate);

        }

        @Test
        void successIfSubscriptionAlreadyExists() throws IOException {

            // Given email

            var email = "jean.dupond@gmail.com";

            // And mock service
            var previousSource = MAPPER.readTree(
                    """
                    {"email": "%s"}
                    """.formatted(email));
            Mockito.when(service.get(email)).thenReturn(Optional.of(previousSource));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "subscription:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            var source = MAPPER.readTree(
                    """
                    {"lastname": "dupond", "firstname":"jean"}
                    """);

            var response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

            // And check service

            var actualSubscription = ArgumentCaptor.forClass(JsonNode.class);

            Mockito.verify(service).update(Mockito.eq(email), actualSubscription.capture());
            assertThat(actualSubscription.getValue()).isEqualTo(source);

            // And check validation

            var sourceToValidate = MAPPER.readTree(
                    """
                    {"email": "%s", "lastname": "dupond", "firstname":"jean"}
                    """.formatted(email));

            Mockito.verify(schemaValidation).validate(Mockito.eq("subscription"), Mockito.eq("v1"), Mockito.anyString(),
                    actualSubscription.capture());
            assertThat(actualSubscription.getValue()).isEqualTo(sourceToValidate);

        }

        @Test
        void isForbidden() throws IOException {

            // Given email

            var email = "jean.dupond@gmail.com";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "subscription:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            var source = MAPPER.readTree(
                    """
                    {"lastname": "dupond", "firstname":"jean"}
                    """);

            var response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .put(Entity.json(source));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).update(any(), any());

        }

    }

    @Nested
    class get {

        @Test
        void success() {

            // Given email

            var email = "jean.dupond@gmail.com";

            // And mock service

            Mockito.when(service.get(email)).thenReturn(Optional.of(subscription.deepCopy()));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "subscription:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            var response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

            // And check body

            assertThat(response.readEntity(JsonNode.class)).isEqualTo(subscription);

        }

        @Test
        void isForbidden() {

            // Given email

            var email = "jean.dupond@gmail.com";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "subscription:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            var response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(service, never()).get(any());

        }
    }

}
