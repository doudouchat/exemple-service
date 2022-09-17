package com.exemple.service.api.core.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.BodyWithContentType;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.StringBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.impl.AuthorizationAlgorithmFactory;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.hazelcast.core.HazelcastInstance;

import io.swagger.v3.oas.annotations.Hidden;

@SpringJUnitConfig({ ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
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

    @Autowired
    private AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    static {
        System.setProperty("mockserver.logLevel", "DEBUG");
    }

    @Value("${api.authorization.port}")
    private int authorizationPort;

    private ClientAndServer authorizationServer;

    protected MockServerClient authorizationClient;

    private static final String URL = "/v1/test";

    @BeforeAll
    public final void authorizationServer() {
        this.authorizationServer = ClientAndServer.startClientAndServer(authorizationPort);
        this.authorizationClient = new MockServerClient("localhost", authorizationPort);
    }

    @AfterAll
    public final void closeMockServer() {

        this.authorizationServer.close();
        this.authorizationServer.hasStopped();
    }

    private static Map<String, String> TOKEN_KEY_CORRECT_RESPONSE = Map.of(
            "alg", "SHA256withRSA",
            "value", "-----BEGIN PUBLIC KEY-----\n"
                    + new String(Base64.encodeBase64(AuthorizationTestConfiguration.PUBLIC_KEY.getEncoded())) + "\n-----END PUBLIC KEY-----");

    private static String TOKEN = JWT.create()
            .withSubject("john_doe")
            .withClaim("client_id", "clientId1")
            .withArrayClaim("scope", new String[] { "test:read" })
            .withJWTId(UUID.randomUUID().toString())
            .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

    @BeforeEach
    private void before() {

        authorizationAlgorithmFactory.resetAlgorithm();
        authorizationClient.reset();

        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().clientId("clientId1").build()));

        testFilter.context = null;

    }

    @Test
    @DisplayName("fails because application is not found")
    void failsBecauseApplicationIsNotFound() {

        // Given mock client

        Map<String, String> tokenKeyResponse = Map.of(
                "alg", "SHA256withRSA",
                "value", "-----BEGIN PUBLIC KEY-----\n"
                        + new String(Base64.encodeBase64(AuthorizationTestConfiguration.PUBLIC_KEY.getEncoded())) + "\n-----END PUBLIC KEY-----");

        authorizationClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/oauth/token_key"))
                .respond(HttpResponse.response()
                        .withBody(JsonBody.json(tokenKeyResponse))
                        .withStatusCode(200));

        // And build token

        String token = JWT.create()
                .withClaim("client_id", "clientId1")
                .withArrayClaim("scope", new String[] { "test:read" })
                .withJWTId(UUID.randomUUID().toString())
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock application information

        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.empty());

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token)
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("test is forbidden");

    }

    @Test
    @DisplayName("fails because token client id and application are different")
    void failsBecauseTokenClientIdAndApplicationAreDifferent() {

        // Given mock client

        Map<String, String> tokenKeyResponse = Map.of(
                "alg", "SHA256withRSA",
                "value", "-----BEGIN PUBLIC KEY-----\n"
                        + new String(Base64.encodeBase64(AuthorizationTestConfiguration.PUBLIC_KEY.getEncoded())) + "\n-----END PUBLIC KEY-----");

        authorizationClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/oauth/token_key"))
                .respond(HttpResponse.response()
                        .withBody(JsonBody.json(tokenKeyResponse))
                        .withStatusCode(200));

        // And build token

        String token = JWT.create()
                .withClaim("client_id", "clientId2")
                .withArrayClaim("scope", new String[] { "test:read" })
                .withJWTId(UUID.randomUUID().toString())
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock application information

        Mockito.when(applicationDetailService.get("test")).thenReturn(Optional.of(ApplicationDetail.builder().clientId("clientId1").build()));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token)
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("clientId2 is forbidden");

    }

    @Test
    @DisplayName("fails because token is in black list")
    void failsBecauseTokenIsInBlackList() {

        // Given mock client

        Map<String, String> tokenKeyResponse = Map.of(
                "alg", "SHA256withRSA",
                "value", "-----BEGIN PUBLIC KEY-----\n"
                        + new String(Base64.encodeBase64(AuthorizationTestConfiguration.PUBLIC_KEY.getEncoded())) + "\n-----END PUBLIC KEY-----");

        authorizationClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/oauth/token_key"))
                .respond(HttpResponse.response()
                        .withBody(JsonBody.json(tokenKeyResponse))
                        .withStatusCode(200));

        // And build token

        String deprecatedTokenId = UUID.randomUUID().toString();
        hazelcastInstance.getMap(AuthorizationTokenManager.TOKEN_BLACK_LIST).put(deprecatedTokenId, Date.from(Instant.now()));
        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(deprecatedTokenId)
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", token)
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo(deprecatedTokenId + " has been excluded");

    }

    @Test
    @DisplayName("fails because public key doesn't check signature")
    void failsBecausePublicKeyDoesntCheckSignature() {

        // Given mock client

        Map<String, String> tokenKeyResponse = Map.of(
                "alg", "SHA256withRSA",
                "value", "-----BEGIN PUBLIC KEY-----\n"
                        + new String(Base64.encodeBase64(AuthorizationTestConfiguration.OTHER_PUBLIC_KEY.getEncoded()))
                        + "\n-----END PUBLIC KEY-----");

        authorizationClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/oauth/token_key"))
                .respond(HttpResponse.response()
                        .withBody(JsonBody.json(tokenKeyResponse))
                        .withStatusCode(200));

        // When perform get

        Response response = target(URL).request(MediaType.APPLICATION_JSON)
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", TOKEN)
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("Signature doesn't match");

    }

    private static Stream<Arguments> authorizedFailureAlgorithm() {

        Map<String, String> TOKEN_KEY_INCORRECT_RESPONSE = Map.of(
                "alg", "SHA256withRSA",
                "value", "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.encodeBase64("123".getBytes())) + "\n-----END PUBLIC KEY-----");

        return Stream.of(
                Arguments.of(JsonBody.json(TOKEN_KEY_CORRECT_RESPONSE), Status.BAD_REQUEST, ClientErrorException.class),
                Arguments.of(JsonBody.json(TOKEN_KEY_INCORRECT_RESPONSE), Status.OK, InvalidKeySpecException.class),
                Arguments.of(new StringBody("", org.mockserver.model.MediaType.APPLICATION_JSON), Status.OK, IllegalArgumentException.class),
                Arguments.of(new StringBody("toto", org.mockserver.model.MediaType.TEXT_PLAIN), Status.OK, ResponseProcessingException.class));
    }

    @ParameterizedTest
    @MethodSource
    void authorizedFailureAlgorithm(BodyWithContentType<?> body, Status status, Class<? extends Throwable> expectedCause) {

        // Given mock client
        authorizationClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/oauth/token_key"))
                .respond(HttpResponse.response()
                        .withHeaders(new Header("Content-Type", body.getContentType()))
                        .withBody(body)
                        .withStatusCode(status.getStatusCode()));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", TOKEN)
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());

        // And check security context

        assertThat(testFilter.context).isNull();

    }

    @Test
    void success() {

        // Given build response
        JsonBody body = JsonBody.json(TOKEN_KEY_CORRECT_RESPONSE);

        // And mock client
        authorizationClient.when(HttpRequest.request()
                .withMethod("GET")
                .withPath("/oauth/token_key"))
                .respond(HttpResponse.response()
                        .withHeaders(new Header("Content-Type", body.getContentType()))
                        .withBody(body)
                        .withStatusCode(200));

        // When perform get

        Response response = target(URL).request()
                .header(SchemaBeanParam.APP_HEADER, "test").header("Authorization", TOKEN)
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
