package com.exemple.service.api.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupportSecure;
import com.exemple.service.api.core.authorization.AuthorizationException;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TestFilter;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.model.LoginEntity;
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
    private AuthorizationTokenValidation authorizationTokenValidation;

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private JsonNode account;

    @BeforeEach
    private void before() {

        Mockito.reset(accountService);
        Mockito.reset(loginResource);
        Mockito.reset(authorizationTokenValidation);

    }

    private static UUID ID = UUID.randomUUID();

    private static Optional<LoginEntity> ID_RESPONSE_LOGIN;

    private static Optional<LoginEntity> RANDOM_RESPONSE_LOGIN;

    static {

        LoginEntity login = new LoginEntity();
        login.setId(ID);

        ID_RESPONSE_LOGIN = Optional.of(login);

        LoginEntity random = new LoginEntity();
        random.setId(UUID.randomUUID());

        RANDOM_RESPONSE_LOGIN = Optional.of(random);
    }

    private Stream<Arguments> authorizedGetUserFailure() {

        String token1 = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:write" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        String token2 = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        return Stream.of(
                Arguments.of(token1, ID_RESPONSE_LOGIN),
                Arguments.of(token2, RANDOM_RESPONSE_LOGIN),
                Arguments.of(token2, Optional.empty()));
    }

    @ParameterizedTest
    @MethodSource
    public void authorizedGetUserFailure(String token, Optional<LoginEntity> loginResponse) {

        // Given mock service
        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(loginResponse);

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

    @Test
    public void authorizedGetUserFailures() throws AuthorizationException {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.doThrow(new AuthorizationException(Response.Status.FORBIDDEN, "error")).when(authorizationTokenValidation)
                .checkSignature(Mockito.any());

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

    @Test
    public void authorizedGetAccount() {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(accountService.get(Mockito.eq(ID))).thenReturn(Optional.of(account));
        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(ID_RESPONSE_LOGIN);

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe")),
                () -> assertThat(testFilter.context.isSecure(), is(true)),
                () -> assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH)));

    }

    @Test
    public void authorizedPostAccount() {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withArrayClaim("scope", new String[] { "account:create" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service

        Mockito.when(accountService.save(Mockito.any(JsonNode.class))).thenReturn(this.account);

        // When perform post

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupond@gmail.com");

        Response response = target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token)
                .post(Entity.json(model));

        // Then check status

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName(), is("clientId1")),
                () -> assertThat(testFilter.context.isSecure(), is(true)),
                () -> assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH)));

    }

    @Test
    public void authorizedGetUser() {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(accountService.get(Mockito.eq(ID))).thenReturn(Optional.of(account));
        Mockito.when(loginResource.get(Mockito.eq("john_doe"))).thenReturn(ID_RESPONSE_LOGIN);

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe")),
                () -> assertThat(testFilter.context.isSecure(), is(true)),
                () -> assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH)));

    }

}
