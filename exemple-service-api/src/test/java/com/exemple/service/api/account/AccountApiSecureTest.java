package com.exemple.service.api.account;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.exemple.service.customer.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

class AccountApiSecureTest extends JerseySpringSupportSecure {

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

    private static final Optional<UUID> ID_RESPONSE_LOGIN = Optional.of(ID);

    private static final Optional<UUID> RANDOM_RESPONSE_LOGIN = Optional.of(UUID.randomUUID());

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
    void authorizedGetUserFailure(String token, Optional<UUID> loginResponse) {

        // Given mock service
        Mockito.when(loginResource.get("john_doe")).thenReturn(loginResponse);

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

    }

    @Test
    void authorizedGetUserFailures() throws AuthorizationException {

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

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

    }

    @Test
    void authorizedGetAccount() {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(accountService.get(ID)).thenReturn(Optional.of(account));
        Mockito.when(loginResource.get("john_doe")).thenReturn(ID_RESPONSE_LOGIN);

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName()).isEqualTo("john_doe"),
                () -> assertThat(testFilter.context.isSecure()).isTrue(),
                () -> assertThat(testFilter.context.getAuthenticationScheme()).isEqualTo(SecurityContext.BASIC_AUTH));

    }

    @Test
    void authorizedPostAccount() {

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

        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName()).isEqualTo("clientId1"),
                () -> assertThat(testFilter.context.isSecure()).isTrue(),
                () -> assertThat(testFilter.context.getAuthenticationScheme()).isEqualTo(SecurityContext.BASIC_AUTH));

    }

    @Test
    void authorizedGetUser() {

        // Given token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe")
                .withArrayClaim("scope", new String[] { "account:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(accountService.get(ID)).thenReturn(Optional.of(account));
        Mockito.when(loginResource.get("john_doe")).thenReturn(ID_RESPONSE_LOGIN);

        // When perform get

        Response response = target(AccountApiTest.URL + "/" + ID).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName()).isEqualTo("john_doe"),
                () -> assertThat(testFilter.context.isSecure()).isTrue(),
                () -> assertThat(testFilter.context.getAuthenticationScheme()).isEqualTo(SecurityContext.BASIC_AUTH));

    }

}
