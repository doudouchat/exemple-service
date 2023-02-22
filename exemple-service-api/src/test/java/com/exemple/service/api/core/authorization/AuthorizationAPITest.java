package com.exemple.service.api.core.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.hazelcast.core.HazelcastInstance;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;

@SpringBootTest(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles("test")
class AuthorizationAPITest extends JerseySpringSupport {

    private TestFilter testFilter = new TestFilter();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration().register(TestApi.class).register(testFilter);
    }

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private static final String URL = "/v1/test";

    @BeforeEach
    public void before() {

        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().clientId("clientId1").build()));

        testFilter.context = null;

    }

    @Test
    @DisplayName("fails because application is not found")
    void failsBecauseApplicationIsNotFound() throws JOSEException {

        // Given token

        var payload = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId1")
                .claim("scope", new String[] { "test:read" })
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(new RSASSASigner(AuthorizationTestConfiguration.RSA_KEY));

        // And mock application information

        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.empty());

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token.serialize())
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("Access to test is forbidden");

    }

    @Test
    @DisplayName("fails because token client id and application are different")
    void failsBecauseTokenClientIdAndApplicationAreDifferent() throws JOSEException {

        // Given token

        var payload = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId2")
                .claim("scope", new String[] { "test:read" })
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(new RSASSASigner(AuthorizationTestConfiguration.RSA_KEY));

        // And mock application information

        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().clientId("clientId1").build()));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token.serialize())
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("Access to test is forbidden");

    }

    @Test
    @DisplayName("fails because token is in black list")
    void failsBecauseTokenIsInBlackList() throws JOSEException {

        // Given token

        String deprecatedTokenId = UUID.randomUUID().toString();
        hazelcastInstance.getMap(AuthorizationTokenManager.TOKEN_BLACK_LIST).put(deprecatedTokenId, Date.from(Instant.now()));

        var payload = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId1")
                .claim("scope", new String[] { "test:read" })
                .jwtID(deprecatedTokenId)
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(new RSASSASigner(AuthorizationTestConfiguration.RSA_KEY));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token.serialize())
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).endsWith(deprecatedTokenId + " has been excluded");

    }

    @Test
    @DisplayName("fails because public key doesn't check signature")
    void failsBecausePublicKeyDoesntCheckSignature() throws JOSEException {

        // Given token

        var payload = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId1")
                .subject("john_doe")
                .claim("scope", new String[] { "test:read" })
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                payload);
        token.sign(new RSASSASigner(AuthorizationTestConfiguration.OTHER_RSA_KEY));

        // When perform get

        Response response = target(URL).request(MediaType.APPLICATION_JSON)
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token.serialize())
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).endsWith("Invalid signature");

    }

    @Test
    void failsBecauseTokenIsDeprecated() throws JOSEException {

        // Given token

        var payload = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId1")
                .subject("john_doe")
                .claim("scope", new String[] { "test:read" })
                .expirationTime(Date.from(Instant.now().minusSeconds(1)))
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                payload);
        token.sign(new RSASSASigner(AuthorizationTestConfiguration.RSA_KEY));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token.serialize())
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).contains("Jwt expired at ");

    }

    @Test
    @DisplayName("fails because public token is missing")
    void failsBecauseTokenIsMissing() {

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test")
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

    }

    private Stream<Arguments> success() {

        var payload = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId1")
                .subject("john_doe")
                .claim("scope", new String[] { "test:read" })
                .jwtID(UUID.randomUUID().toString())
                .build();

        var payloadWithoutJti = new JWTClaimsSet.Builder()
                .claim("client_id", "clientId1")
                .subject("john_doe")
                .claim("scope", new String[] { "test:read" })
                .build();

        return Stream.of(
                Arguments.of(payload),
                Arguments.of(payloadWithoutJti));
    }

    @ParameterizedTest
    @MethodSource
    void success(JWTClaimsSet payload) throws JOSEException {

        // Given token

        var token = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                payload);
        token.sign(new RSASSASigner(AuthorizationTestConfiguration.RSA_KEY));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token.serialize())
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName()).isEqualTo("john_doe"),
                () -> assertThat(testFilter.context.isSecure()).isTrue(),
                () -> assertThat(testFilter.context.getAuthenticationScheme()).isEqualTo(SecurityContext.BASIC_AUTH));

    }

    @Path("/v1/test")
    @Hidden
    public static class TestApi {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @RolesAllowed("test:read")
        public Response get() {

            return Response.ok().build();

        }

    }

    public static class TestFilter implements ContainerRequestFilter {

        public SecurityContext context;

        @Override
        public void filter(ContainerRequestContext requestContext) {

            context = requestContext.getSecurityContext();

        }

    }

}
