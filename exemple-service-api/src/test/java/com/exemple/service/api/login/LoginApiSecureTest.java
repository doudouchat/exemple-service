package com.exemple.service.api.login;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.exemple.service.customer.login.LoginResource;

class LoginApiSecureTest extends JerseySpringSupportSecure {

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

    @Test
    void authorizedGetLoginSuccess() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // and token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject(username).withArrayClaim("scope", new String[] { "login:read" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service

        Mockito.when(loginResource.get(Mockito.eq(username))).thenReturn(Optional.of(UUID.randomUUID()));

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check security context

        assertAll(
                () -> assertThat(testFilter.context.getUserPrincipal().getName()).isEqualTo(username),
                () -> assertThat(testFilter.context.isSecure()).isTrue(),
                () -> assertThat(testFilter.context.getAuthenticationScheme()).isEqualTo(SecurityContext.BASIC_AUTH));

    }

    @Test
    void authorizedGetLoginFailure() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // and token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("other").withArrayClaim("scope", new String[] { "login:read" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

    }

}
