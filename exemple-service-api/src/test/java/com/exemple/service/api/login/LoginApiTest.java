package com.exemple.service.api.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.login.LoginResource;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

@SpringJUnitConfig(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles("AuthorizationMock")
class LoginApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private LoginResource resource;

    @BeforeEach
    void before() {

        Mockito.reset(resource);

    }

    public static final String URL = "/v1/logins";

    @Nested
    class check {

        @Test
        void success() {

            // Given login

            String username = "jean.dupond@gmail.com";

            // And mock service

            Mockito.when(resource.get(username)).thenReturn(Optional.of(UUID.randomUUID()));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "login:head" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform head

            Response response = target(URL + "/" + username).request()
                    .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token)
                    .head();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

        }

        @Test
        void fails() throws Exception {

            // Given username

            String username = "jean.dupond@gmail.com";

            // And mock service

            Mockito.when(resource.get(username)).thenReturn(Optional.empty());

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "login:head" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform head

            Response response = target(URL + "/" + username).request()
                    .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token)
                    .head();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

        }

        @Test
        void isForbidden() throws Exception {

            // Given username

            String username = "jean.dupond@gmail.com";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "login:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform head

            Response response = target(URL + "/" + username).request()
                    .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token)
                    .head();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        }

    }

    @Nested
    class get {

        @Test
        void success() {

            // Given user_name

            String username = "jean.dupond@gmail.com";

            // And mock service

            UUID id = UUID.randomUUID();

            Mockito.when(resource.get(username)).thenReturn(Optional.of(id));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("scope", new String[] { "login:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // And check status

            assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

            // And check body
            assertThat(response.readEntity(UUID.class)).isEqualTo(id);

        }

        @Test
        void notFound() {

            // Given user_name

            String username = "jean.dupond@gmail.com";

            // And mock service

            Mockito.when(resource.get(username)).thenReturn(Optional.empty());

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("scope", new String[] { "login:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // And check status

            assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

        }

        @Test
        void isForbidden() {

            // Given user_name

            String username = "jean.dupond@gmail.com";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("scope", new String[] { "login:head" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // And check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(resource, never()).get(any());

        }

        @Test
        void subjectTokenIsIncorrect() {

            // Given user_name

            String username = "jean.dupond@gmail.com";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .subject("jean.dupont@gmail.com")
                    .claim("scope", new String[] { "login:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform get

            Response response = target(URL + "/" + username).request(MediaType.APPLICATION_JSON)
                    .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                    .get();

            // And check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // And check service

            Mockito.verify(resource, never()).get(any());

        }

    }

}
