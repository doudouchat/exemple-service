package com.exemple.service.api.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupportSecure;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TestFilter;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.model.LoginEntity;

public class LoginApiSecureTest extends JerseySpringSupportSecure {

    private TestFilter testFilter = new TestFilter();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration().register(testFilter);
    }

    @Autowired
    private LoginResource loginResource;

    @BeforeEach
    private void before() {
        Mockito.reset(loginResource);

    }

    private static UUID ID = UUID.randomUUID();

    @Test
    public void authorizedGetLoginWithPrincipal() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        LoginEntity first = new LoginEntity();
        first.setUsername(username);
        first.setId(ID);

        // and token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject(username).withArrayClaim("scope", new String[] { "login:read" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service

        Mockito.when(loginResource.get(Mockito.eq(username))).thenReturn(Optional.of(first));

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName(), is(username)),
                () -> assertThat(testFilter.context.isSecure(), is(true)),
                () -> assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH)));

    }

    @Test
    public void authorizedGetLoginWithSecond() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        LoginEntity first = new LoginEntity();
        first.setUsername(username);
        first.setId(ID);

        // And second login

        String secondUsername = "jack.dupond@gmail.com";

        LoginEntity second = new LoginEntity();
        second.setUsername(secondUsername);
        second.setId(ID);

        // And token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject(secondUsername)
                .withArrayClaim("scope", new String[] { "login:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(loginResource.get(Mockito.eq(username))).thenReturn(Optional.of(first));
        Mockito.when(loginResource.get(Mockito.eq(secondUsername))).thenReturn(Optional.of(second));

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName(), is(secondUsername)),
                () -> assertThat(testFilter.context.isSecure(), is(true)),
                () -> assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH)));

    }

    @Test
    public void authorizedGetLoginFailure() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // and token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("other").withArrayClaim("scope", new String[] { "login:read" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }

    @Test
    public void authorizedGetLoginWithSecondFailure() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        LoginEntity first = new LoginEntity();
        first.setUsername(username);
        first.setId(ID);

        // And second login

        String secondUsername = "jack.dupond@gmail.com";

        LoginEntity second = new LoginEntity();
        second.setUsername(secondUsername);
        second.setId(UUID.randomUUID());

        // And token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject(secondUsername)
                .withArrayClaim("scope", new String[] { "login:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(loginResource.get(Mockito.eq(username))).thenReturn(Optional.of(first));
        Mockito.when(loginResource.get(Mockito.eq(secondUsername))).thenReturn(Optional.of(second));

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }
}
