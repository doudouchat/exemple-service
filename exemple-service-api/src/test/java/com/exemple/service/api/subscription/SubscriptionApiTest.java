package com.exemple.service.api.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.subscription.SubscriptionService;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

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

    @BeforeEach
    public void before() {

        Mockito.reset(service, schemaValidation);

    }

    public static final String URL = "/v1/subscriptions";

    private static Stream<Arguments> update() {

        return Stream.of(
                // created
                Arguments.of(true, Status.CREATED),
                // updated
                Arguments.of(false, Status.NO_CONTENT));
    }

    @ParameterizedTest
    @MethodSource
    public void update(boolean created, Status expectedStatus) {

        // Given email

        String email = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.save(Mockito.eq(email), Mockito.any(JsonNode.class))).thenReturn(created);

        // When perform put

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("lastname", "dupond");
            model.put("firstname", "jean");

            return model;

        });

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .put(Entity.json(source));

        // Then check status

        assertThat(response.getStatus(), is(expectedStatus.getStatusCode()));

        // And check service

        ArgumentCaptor<JsonNode> subscription = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(service).save(Mockito.eq(email), subscription.capture());
        assertThat(subscription.getValue(), is(source));

        // And check validation

        JsonNode sourceToValidate = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("lastname", "dupond");
            model.put("firstname", "jean");
            model.put("email", email);

            return model;

        });

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("subscription"),
                subscription.capture());
        assertThat(subscription.getValue(), is(sourceToValidate));

    }

    @Test
    public void get() {

        // Given email

        String email = "jean.dupond@gmail.com";

        // And mock service

        Mockito.when(service.get(Mockito.eq(email))).thenReturn(Optional.of(subscription));

        // When perform get

        Response response = target(URL + "/" + email).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(subscription));

    }

}
