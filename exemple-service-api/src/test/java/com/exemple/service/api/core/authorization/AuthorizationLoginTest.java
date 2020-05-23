package com.exemple.service.api.core.authorization;

import static com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.RSA256_ALGORITHM;
import static com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TOKEN_KEY_RESPONSE;
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
import org.mockserver.client.MockServerClient;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.auth0.jwt.JWT;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration.TestFilter;
import com.exemple.service.api.core.authorization.impl.AuthorizationAlgorithmFactory;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.api.login.LoginApiTest;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.login.LoginService;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginResource;

@ActiveProfiles(inheritProfiles = false)
public class AuthorizationLoginTest extends JerseySpringSupport {

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

    private static UUID ID = UUID.randomUUID();

    @Test
    public void authorizedGetLoginWithPrincipal() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "login:read" }).sign(RSA256_ALGORITHM);

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        model.put("id", ID);

        Mockito.when(loginService.get(Mockito.eq("john_doe"))).thenReturn(JsonNodeUtils.create(model));

        Response response = target(LoginApiTest.URL + "/" + "john_doe").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        Mockito.verify(loginService).get(Mockito.eq("john_doe"));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }

    @Test
    public void authorizedGetLoginWithSecond() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "login:read" }).sign(RSA256_ALGORITHM);

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        model.put("id", ID);

        Mockito.when(loginResource.get(Mockito.eq("jack_doe"))).thenReturn(Optional.of(JsonNodeUtils.create(model)));
        Mockito.when(loginResource.get(Mockito.eq(ID)))
                .thenReturn(Collections.singletonList(JsonNodeUtils.create(Collections.singletonMap("username", "john_doe"))));
        Mockito.when(loginService.get(Mockito.eq("jack_doe"))).thenReturn(JsonNodeUtils.create(model));

        Response response = target(LoginApiTest.URL + "/" + "jack_doe").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        Mockito.verify(loginService).get(Mockito.eq("jack_doe"));

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

    @Test
    public void authorizedGetLoginWithSecondFailure() throws Exception {

        String token = JWT.create().withClaim("client_id", "clientId1").withSubject("john_doe").withAudience("exemple")
                .withArrayClaim("scope", new String[] { "login:read" }).sign(RSA256_ALGORITHM);

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        model.put("id", ID);

        Mockito.when(loginResource.get(Mockito.eq("jack_doe"))).thenReturn(Optional.of(JsonNodeUtils.create(model)));
        Mockito.when(loginResource.get(Mockito.eq(ID)))
                .thenReturn(Collections.singletonList(JsonNodeUtils.create(Collections.singletonMap("username", "jack_doe"))));
        Mockito.when(loginService.get(Mockito.eq("jack_doe"))).thenReturn(JsonNodeUtils.create(model));

        Response response = target(LoginApiTest.URL + "/" + "jack_doe").request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").header("Authorization", token).get();

        assertThat(response.getStatus(), is(Status.FORBIDDEN.getStatusCode()));

        assertThat(testFilter.context.getUserPrincipal().getName(), is("john_doe"));
        assertThat(testFilter.context.isSecure(), is(true));
        assertThat(testFilter.context.getAuthenticationScheme(), is(SecurityContext.BASIC_AUTH));

    }
}
