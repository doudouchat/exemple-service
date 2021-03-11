package com.exemple.service.api.account;

import static com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.RSA256_ALGORITHM;
import static com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TOKEN_KEY_RESPONSE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupportSecure;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TestFilter;
import com.exemple.service.api.core.authorization.impl.AuthorizationAlgorithmFactory;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

public class AccountApiSecureTest extends JerseySpringSupportSecure {

    private TestFilter testFilter = new TestFilter();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration().register(testFilter);
    }

    @Autowired
    private AccountService accountService;

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    @Autowired
    private JsonNode account;

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

    private static UUID ID = UUID.randomUUID();

    private static Optional<JsonNode> ID_RESPONSE_LOGIN = Optional.of(JsonNodeUtils.create(() -> Collections.singletonMap("id", ID)));

    private static Optional<JsonNode> RANDOM_RESPONSE_LOGIN = Optional
            .of(JsonNodeUtils.create(() -> Collections.singletonMap("id", UUID.randomUUID())));

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

                { token1, ID_RESPONSE_LOGIN },

                { token2, RANDOM_RESPONSE_LOGIN },

                { token2, Optional.empty() },

                { token3, ID_RESPONSE_LOGIN },

                { token4, ID_RESPONSE_LOGIN }

        };
    }

    @Test(dataProvider = "notAuthorized")
    public void authorizedGetUserFailure(String token, Optional<JsonNode> loginResponse) {

        // Given mock service

        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(loginResponse);

        // And perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

    @Test
    public void authorizedGetAccount() throws AccountServiceNotFoundException {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(accountService.get(Mockito.eq(ID))).thenReturn(account);
        Mockito.when(loginResource.get(Mockito.eq("john_doe")))
                .thenReturn(Optional.of(JsonNodeUtils.create(() -> Collections.singletonMap("id", ID))));

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedPostAccount() {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:create" }).sign(RSA256_ALGORITHM);

        // And mock service

        Mockito.when(accountService.save(Mockito.any(JsonNode.class))).thenReturn(this.account);

        // When perform post

        Response response = target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                .post(Entity.json(account.toString()));

        // Then check status

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

        // And check security context

        assertThat(testFilter.context.getUserPrincipal().getName(), is("clientId1"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedGetUser() throws AccountServiceNotFoundException {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(accountService.get(Mockito.eq(ID))).thenReturn(account);
        Mockito.when(loginResource.get(Mockito.eq("john_doe")))
                .thenReturn(Optional.of(JsonNodeUtils.create(() -> Collections.singletonMap("id", ID))));

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

}
