package com.exemple.service.api.common;

import static com.exemple.service.api.common.security.ApiProfile.USER_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.api.account.AccountApiTest;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceException;

public class ExceptionApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @BeforeMethod
    private void before() {

        Mockito.reset(service);

    }

    @Test
    public void notFound() {

        Response response = target("/v1/notfound").request(MediaType.APPLICATION_JSON).get();

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

    @Test
    public void notAcceptable() {

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).request(MediaType.TEXT_HTML).get();

        assertThat(response.getStatus(), is(Status.NOT_ACCEPTABLE.getStatusCode()));

    }

    @Test
    public void JsonException() {

        Response response = target(AccountApiTest.URL).request(MediaType.APPLICATION_JSON).post(Entity.json("toto"));

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

    }

    @Test
    public void JsonEmptyException() {

        Response response = target(AccountApiTest.URL + "/" + UUID.randomUUID()).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(Collections.EMPTY_LIST));

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

    }

    @Test
    public void internalServerError() throws AccountServiceException {

        UUID id = UUID.randomUUID();

        Mockito.when(service.get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile)))
                .thenThrow(new AccountServiceException());

        Response response = target(AccountApiTest.URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1").get();

        Mockito.verify(service).get(Mockito.eq(id), Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq(USER_PROFILE.profile));

        assertThat(response.getStatus(), is(Status.INTERNAL_SERVER_ERROR.getStatusCode()));

    }

}
