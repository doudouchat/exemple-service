package com.exemple.service.api.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.customer.subscription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class SubscriptionApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private SubscriptionService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private JsonNode subscription;

    @BeforeMethod
    public void before() {

        Mockito.reset(service, schemaValidation);

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
    public void update(boolean created, Status expectedStatus) {

        // Given email

        String email = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.save(Mockito.any(JsonNode.class))).thenReturn(created);

        // When perform put

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("lastname", "dupond");
            model.put("firstname", "jean");

            return model;

        });

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .put(Entity.json(source.toString()));

        // Then check status

        assertThat(response.getStatus(), is(expectedStatus.getStatusCode()));

        // And check service

        ArgumentCaptor<JsonNode> subscription = ArgumentCaptor.forClass(JsonNode.class);

        JsonNode expectedSubscription = JsonNodeUtils.set(source, "email", new TextNode(email));

        Mockito.verify(service).save(subscription.capture());
        assertThat(subscription.getValue(), is(expectedSubscription));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("subscription"),
                subscription.capture());
        assertThat(subscription.getValue(), is(expectedSubscription));

    }

    @Test
    public void get() throws SubscriptionServiceNotFoundException {

        // Given email

        String email = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(email))).thenReturn(subscription);

        // When perform get

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(subscription));

    }

    @Test
    public void getNotFound() throws SubscriptionServiceNotFoundException {

        // Given email

        String email = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(email))).thenThrow(new SubscriptionServiceNotFoundException());

        // When perform get

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

}
