package com.exemple.service.customer.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.JsonNodeUtils;
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

@SpringJUnitConfig(CustomerTestConfiguration.class)
public class SubscriptionServiceTest {

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private ResourceEventPublisher publisher;

    @Autowired
    private SubscriptionService service;

    @BeforeEach
    private void before() {

        Mockito.reset(resource, publisher);

    }

    @BeforeAll
    public static void initServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
    }

    private static Stream<Arguments> save() {

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            return model;

        });

        return Stream.of(
                Arguments.of(Optional.empty(), true),
                Arguments.of(Optional.of(JsonNodeUtils.create(() -> source)), false));
    }

    @DisplayName("save subscription")
    @ParameterizedTest
    @MethodSource
    public void save(Optional<JsonNode> subscription, boolean expectedCreated) {

        // Given email

        String email = "jean@gmail.com";

        // And mock resource

        Mockito.when(resource.get(email)).thenReturn(subscription);

        // When perform save

        JsonNode source = JsonNodeUtils.create(Collections::emptyMap);

        boolean created = service.save(email, source);

        // Then check subscription

        assertThat(created, is(expectedCreated));

        // And check save resource

        JsonNode expectedSubscription = JsonNodeUtils.set(source, "subscription_date",
                new TextNode(ServiceContextExecution.context().getDate().toString()));

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue(), is(expectedSubscription));

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("subscription"), Mockito.eq(EventType.CREATE));
        assertThat(eventCaptor.getValue(), is(expectedSubscription));

    }

    @DisplayName("get subscription")
    @Test
    public void get() {

        // Given email

        String email = "jean@gmail.com";

        // And mock resource

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            return model;

        });

        Mockito.when(resource.get(email)).thenReturn(Optional.of(source));

        // When perform get

        Optional<JsonNode> subscription = service.get(email);

        // Then check subscription

        assertThat(subscription.get(), is(source));

    }

}
