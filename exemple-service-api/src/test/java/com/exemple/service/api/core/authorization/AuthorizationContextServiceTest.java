package com.exemple.service.api.core.authorization;

import static com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.RSA256_ALGORITHM;
import static com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TOKEN_KEY_RESPONSE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.authorization.impl.AuthorizationAlgorithmFactory;
import com.hazelcast.core.HazelcastInstance;

@ContextConfiguration(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
public class AuthorizationContextServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AuthorizationContextService service;

    @Autowired
    private AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    @Autowired
    private HazelcastInstance hazelcastInstance;

    private static final UUID DEPRECATED_TOKEN_ID = UUID.randomUUID();

    @Autowired
    private MockServerClient authorizationClient;

    @BeforeMethod
    private void before() {

        authorizationAlgorithmFactory.resetAlgorithm();

        hazelcastInstance.getMap(AuthorizationConfiguration.TOKEN_BLACK_LIST).put(DEPRECATED_TOKEN_ID.toString(), Date.from(Instant.now()));

        authorizationClient.reset();
    }

    @DataProvider(name = "authorizedFailure")
    private static Object[][] failure() {

        Map<String, String> tokenKeyFailure = new HashMap<>();
        tokenKeyFailure.put("alg", "SHA256withRSA");
        tokenKeyFailure.put("value",
                "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.encodeBase64("123".getBytes())) + "\n-----END PUBLIC KEY-----");

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(UUID.randomUUID().toString()).sign(RSA256_ALGORITHM);

        String other = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("other")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(UUID.randomUUID().toString()).sign(RSA256_ALGORITHM);

        String deprecatedToken = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(DEPRECATED_TOKEN_ID.toString()).sign(RSA256_ALGORITHM);

        return new Object[][] {

                { token, TOKEN_KEY_RESPONSE, Status.BAD_REQUEST },

                { token, tokenKeyFailure, Status.OK },

                { other, TOKEN_KEY_RESPONSE, Status.OK },

                { deprecatedToken, TOKEN_KEY_RESPONSE, Status.OK },

        };
    }

    @Test(dataProvider = "authorizedFailure", expectedExceptions = AuthorizationException.class)
    public void authorizedFailure(String token, Map<String, String> tokenKey, Status status) throws AuthorizationException {

        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(tokenKey)).withStatusCode(status.getStatusCode()));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        service.buildContext(headers);

    }

    @Test
    public void authorized() throws AuthorizationException {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:write" }).withJWTId(UUID.randomUUID().toString()).sign(RSA256_ALGORITHM);

        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(TOKEN_KEY_RESPONSE)).withStatusCode(200));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        ApiSecurityContext securityContext = service.buildContext(headers);

        assertThat(securityContext.getUserPrincipal().getName(), is("john_doe"));
        assertThat(securityContext.isSecure(), is(true));
        assertThat(securityContext.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test(expectedExceptions = AuthorizationException.class)
    public void singleUse() throws AuthorizationException {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:write" }).withClaim("singleUse", true).withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))).sign(RSA256_ALGORITHM);

        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(TOKEN_KEY_RESPONSE)).withStatusCode(200));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        ApiSecurityContext securityContext = service.buildContext(headers);
        service.cleanContext(securityContext, Status.OK);
        service.buildContext(headers);

    }

    @Test
    public void singleUseFailure() throws AuthorizationException {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:write" }).withClaim("singleUse", true).withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS))).sign(RSA256_ALGORITHM);

        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(TOKEN_KEY_RESPONSE)).withStatusCode(200));

        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        headers.putSingle("Authorization", "Bearer " + token);
        headers.putSingle(ApplicationBeanParam.APP_HEADER, "test");

        ApiSecurityContext securityContext = service.buildContext(headers);
        service.cleanContext(securityContext, Status.BAD_REQUEST);
        securityContext = service.buildContext(headers);

        assertThat(securityContext.getUserPrincipal().getName(), is("john_doe"));
        assertThat(securityContext.isSecure(), is(true));
        assertThat(securityContext.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

}
