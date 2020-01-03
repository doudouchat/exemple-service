package com.exemple.service.api.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.subcription.SubscriptionService;
import com.exemple.service.customer.subcription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;

public class SubscriptionApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private SubscriptionService service;

    @BeforeMethod
    public void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/subscriptions";

    @DataProvider(name = "update")
    private static Object[][] update() {

        return new Object[][] {
                // created
                { true, Status.CREATED },
                // updated
                { false, Status.NO_CONTENT }
                //
        };
    }

    @Test(dataProvider = "update")
    public void update(boolean created, Status expectedStatus) throws Exception {

        String email = "jean.dupond@gmail.com";

        Mockito.when(service.save(Mockito.eq(email), Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(created);

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .put(Entity.json(JsonNodeUtils.init().toString()));

        Mockito.verify(service).save(Mockito.eq(email), Mockito.any(JsonNode.class), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(expectedStatus.getStatusCode()));

    }

    @Test
    public void get() throws Exception {

        String email = "jean.dupond@gmail.com";

        Mockito.when(service.get(Mockito.eq(email), Mockito.eq("test"), Mockito.eq("v1"))).thenReturn(JsonNodeUtils.init());

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(email), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

    }

    @Test
    public void getFailure() throws Exception {

        String email = "jean.dupond@gmail.com";

        Mockito.when(service.get(Mockito.eq(email), Mockito.eq("test"), Mockito.eq("v1"))).thenThrow(new SubscriptionServiceNotFoundException());

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        Mockito.verify(service).get(Mockito.eq(email), Mockito.eq("test"), Mockito.eq("v1"));

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

}
