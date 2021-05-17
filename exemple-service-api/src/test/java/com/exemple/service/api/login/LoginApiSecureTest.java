package com.exemple.service.api.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupportSecure;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TestFilter;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;

public class LoginApiSecureTest extends JerseySpringSupportSecure {

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
    private JsonNode login;

    @BeforeMethod
    private void before() {

        Mockito.reset(accountService);
        Mockito.reset(loginResource);

    }

    private static UUID ID = UUID.randomUUID();

    @Test
    public void authorizedGetLoginWithPrincipal() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // and token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject(username).withArrayClaim("scope", new String[] { "login:read" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service

        Mockito.when(loginService.get(Mockito.eq(username))).thenReturn(login);

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertThat(testFilter.context.getUserPrincipal().getName(), is(username));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedGetLoginWithSecond() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And second login

        String secondUsername = "jack.dupond@gmail.com";

        JsonNode second = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", secondUsername);
            model.put("password", "jean.dupont");
            model.put("id", ID);

            return model;
        });

        // And token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject(secondUsername)
                .withArrayClaim("scope", new String[] { "login:read" }).sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(loginResource.get(Mockito.eq(username))).thenReturn(Optional.of(JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("password", "jean.dupont");
            model.put("id", ID);

            return model;
        })));
        Mockito.when(loginResource.get(Mockito.eq(ID))).thenReturn(Collections.singletonList(second));
        Mockito.when(loginService.get(Mockito.eq(username))).thenReturn(login);

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check security context

        assertThat(testFilter.context.getUserPrincipal().getName(), is(secondUsername));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

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
    public void authorizedGetLoginWithSecondFailure() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And second login

        String secondUsername = "jack.dupond@gmail.com";

        JsonNode second = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", secondUsername);
            model.put("password", "jean.dupont");
            model.put("id", ID);

            return model;
        });

        // And token

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("other").withArrayClaim("scope", new String[] { "login:read" })
                .sign(AuthorizationTestConfiguration.RSA256_ALGORITHM);

        // And mock service & resource

        Mockito.when(loginResource.get(Mockito.eq(username))).thenReturn(Optional.of(JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("password", "jean.dupont");
            model.put("id", ID);

            return model;
        })));
        Mockito.when(loginResource.get(Mockito.eq(ID))).thenReturn(Collections.singletonList(second));

        // When perform get

        Response response = target(LoginApiTest.URL + "/" + username).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

    }
}
