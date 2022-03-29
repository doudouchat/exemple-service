package com.exemple.service.api.core.authorization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.authorization.impl.AuthorizationAlgorithmFactory;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.hazelcast.core.HazelcastInstance;

@SpringJUnitConfig({ ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthorizationContextServiceTest {

    @Autowired
    private AuthorizationContextService service;

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

    private static final UUID DEPRECATED_TOKEN_ID = UUID.randomUUID();

    private static Map<String, String> TOKEN_KEY_CORRECT_RESPONSE = new HashMap<>();

    private static Map<String, String> TOKEN_KEY_OTHER_RESPONSE = new HashMap<>();

    private static Map<String, String> TOKEN_KEY_INCORRECT_RESPONSE = new HashMap<>();

    static {

        TOKEN_KEY_CORRECT_RESPONSE.put("alg", "SHA256withRSA");
        TOKEN_KEY_CORRECT_RESPONSE.put("value", "-----BEGIN PUBLIC KEY-----\n"
                + new String(Base64.encodeBase64(AuthorizationTestConfiguration.PUBLIC_KEY.getEncoded())) + "\n-----END PUBLIC KEY-----");

        TOKEN_KEY_OTHER_RESPONSE.put("alg", "SHA256withRSA");
        TOKEN_KEY_OTHER_RESPONSE.put("value", "-----BEGIN PUBLIC KEY-----\n"
                + new String(Base64.encodeBase64(AuthorizationTestConfiguration.OTHER_PUBLIC_KEY.getEncoded())) + "\n-----END PUBLIC KEY-----");

        TOKEN_KEY_INCORRECT_RESPONSE.put("alg", "SHA256withRSA");
        TOKEN_KEY_INCORRECT_RESPONSE.put("value",
                "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.encodeBase64("123".getBytes())) + "\n-----END PUBLIC KEY-----");
    }

    @BeforeEach
    private void before() {

        authorizationAlgorithmFactory.resetAlgorithm();

        hazelcastInstance.getMap(AuthorizationTokenManager.TOKEN_BLACK_LIST).put(DEPRECATED_TOKEN_ID.toString(), Date.from(Instant.now()));
        authorizationClient.reset();

        Mockito.when(applicationDetailService.get("test")).thenReturn(ApplicationDetail.builder().clientId("clientId1").build());

    }

    private static Stream<Arguments> authorizedFailure() {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(UUID.randomUUID().toString())
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        String badClientIdToken = JWT.create().withClaim("client_id", "clientId2").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(UUID.randomUUID().toString())
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        String deprecatedToken = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(DEPRECATED_TOKEN_ID.toString())
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        return Stream.of(
                Arguments.of(token, TOKEN_KEY_CORRECT_RESPONSE, Status.BAD_REQUEST),
                Arguments.of(token, TOKEN_KEY_OTHER_RESPONSE, Status.OK),
                Arguments.of(badClientIdToken, TOKEN_KEY_CORRECT_RESPONSE, Status.OK),
                Arguments.of(token, TOKEN_KEY_INCORRECT_RESPONSE, Status.OK),
                Arguments.of(deprecatedToken, TOKEN_KEY_CORRECT_RESPONSE, Status.OK));
    }

    @ParameterizedTest
    @MethodSource
    public void authorizedFailure(String token, Map<String, String> tokenKey, Status status) {

        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(tokenKey)).withStatusCode(status.getStatusCode()));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        // When perform
        Throwable throwable = catchThrowable(() -> service.buildContext(headers));

        // Then check throwable
        assertThat(throwable).isInstanceOf(AuthorizationException.class);

    }

    @Test
    public void authorized() throws AuthorizationException {

        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(TOKEN_KEY_CORRECT_RESPONSE)).withStatusCode(200));

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(UUID.randomUUID().toString())
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        // When perform
        ApiSecurityContext securityContext = service.buildContext(headers);

        // Then check security context
        assertAll(
                () -> assertThat(securityContext.getUserPrincipal().getName()).isEqualTo("john_doe"),
                () -> assertThat(securityContext.isSecure()).isTrue(),
                () -> assertThat(securityContext.getAuthenticationScheme()).isEqualTo((SecurityContext.BASIC_AUTH)));

    }

}
