package com.exemple.service.api.core.authorization;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.codec.binary.Base64;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.service.api.account.AccountApiTest;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.impl.AuthorizationAlgorithmFactory;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.api.login.LoginApiTest;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

@ActiveProfiles(inheritProfiles = false)
public class AuthorizationTest extends JerseySpringSupport {

    private TestFilter testFilter = new TestFilter();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration().register(testFilter);
    }

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    @Autowired
    private MockServerClient authorizationClient;

    @BeforeMethod
    private void before() {

        Mockito.reset(accountService, accountService);
        Mockito.reset(loginResource);

        authorizationAlgorithmFactory.resetAlgorithm();

        authorizationClient.reset();
        authorizationClient.when(HttpRequest.request().withMethod("GET").withPath("/oauth/token_key"))
                .respond(HttpResponse.response().withHeaders(new Header("Content-Type", "application/json;charset=UTF-8"))
                        .withBody(JsonBody.json(TOKEN_KEY_RESPONSE)).withStatusCode(200));

    }

    private static final Algorithm RSA256_ALGORITHM;

    private static final Map<String, Object> TOKEN_KEY_RESPONSE = new HashMap<>();

    static {

        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        keyPairGenerator.initialize(1024);
        KeyPair keypair = keyPairGenerator.genKeyPair();
        PrivateKey privateKey = keypair.getPrivate();
        PublicKey publicKey = keypair.getPublic();

        RSA256_ALGORITHM = Algorithm.RSA256((RSAPublicKey) publicKey, (RSAPrivateKey) privateKey);

        TOKEN_KEY_RESPONSE.put("alg", "SHA256withRSA");
        TOKEN_KEY_RESPONSE.put("value",
                "-----BEGIN PUBLIC KEY-----\n" + new String(Base64.encodeBase64(publicKey.getEncoded())) + "\n-----END PUBLIC KEY-----");

    }

    private static UUID ID = UUID.randomUUID();

    @DataProvider(name = "notAuthorized")
    private static Object[][] notAuthorized() {

        String token1 = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:write" }).sign(RSA256_ALGORITHM);

        String token2 = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        String token3 = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("other")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        String token4 = JWT.create().withClaim("client_id", "clientId2").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        return new Object[][] {

                { token1, ID },

                { token2, UUID.randomUUID() },

                { token3, ID },

                { token4, ID }

        };
    }

    @Test(dataProvider = "notAuthorized")
    public void authorizedGetUserFailure(String token, UUID id) throws Exception {

        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(Optional.of(JsonNodeUtils.create(Collections.singletonMap("id", id))));

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

    @Test
    public void authorizedGetAccount() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        Mockito.when(accountService.get(Mockito.eq(ID), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.init("email"));
        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(Optional.of(JsonNodeUtils.create(Collections.singletonMap("id", ID))));

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        Mockito.verify(accountService).get(Mockito.eq(ID), Mockito.eq("test"), Mockito.eq("v1"));
        Mockito.verify(loginResource).get(Mockito.eq("john_doe"));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedPostAccount() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:create" }).sign(RSA256_ALGORITHM);

        Mockito.when(accountService.save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1")))
                .thenReturn(JsonNodeUtils.create(Collections.singletonMap("id", UUID.randomUUID())));

        Response response = target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                .post(Entity.json(JsonNodeUtils.init("email").toString()));

        Mockito.verify(accountService).save(Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("clientId1"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedGetUser() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        Mockito.when(accountService.get(Mockito.eq(ID), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.init("email"));
        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(Optional.of(JsonNodeUtils.create(Collections.singletonMap("id", ID))));

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        Mockito.verify(accountService).get(Mockito.eq(ID), Mockito.eq("test"), Mockito.eq("v1"));
        Mockito.verify(loginResource).get(Mockito.eq("john_doe"));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedGetLogin() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "login:read" }).sign(RSA256_ALGORITHM);

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        model.put("id", UUID.randomUUID());

        Mockito.when(loginService.get(Mockito.eq("john_doe"), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.create(model));

        Response response = target(LoginApiTest.URL + "/" + "john_doe").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        Mockito.verify(loginService).get(Mockito.eq("john_doe"), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedGetLoginFailure() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "login:read" }).sign(RSA256_ALGORITHM);

        Response response = target(LoginApiTest.URL + "/" + "other").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    public static class TestFilter implements ContainerRequestFilter {

        SecurityContext context;

        @Override
        public void filter(ContainerRequestContext requestContext) {

            context = requestContext.getSecurityContext();

        }

    }
}
