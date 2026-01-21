package com.exemple.service.customer.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.context.SubscriptionContextExecution;
import com.exemple.service.customer.core.CustomerTestConfiguration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

@SpringJUnitConfig(CustomerTestConfiguration.class)
class SubscriptionServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private SubscriptionService service;

    @BeforeEach
    void before() {

        Mockito.reset(resource);

    }

    @BeforeAll
    static void initServiceContextExecution() {

        ServiceContextExecution.setApp("default");
    }

    @Test
    void create() {

        // Given email

        String email = "jean@gmail.com";

        // When perform save

        JsonNode source = MAPPER.createObjectNode();

        service.create(email, source);

        // Then check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).set("subscription_date",
                StringNode.valueOf(ServiceContextExecution.context().getDate().toString()));

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).create(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue()).isEqualTo(expectedSubscription);

    }

    @Test
    void update() {

        // Given email

        String email = "jean@gmail.com";

        // When perform save

        JsonNode source = MAPPER.createObjectNode();
        JsonNode previousSource = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com"}
                """);
        SubscriptionContextExecution.setPreviousSubscription(previousSource);

        service.update(email, source);

        // Then check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).set("subscription_date",
                StringNode.valueOf(ServiceContextExecution.context().getDate().toString()));

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).update(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue()).isEqualTo(expectedSubscription);

    }

    @DisplayName("get subscription")
    @Test
    void get() {

        // Given email

        String email = "jean@gmail.com";

        // And mock resource

        JsonNode source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com"}
                """);

        Mockito.when(resource.get(email)).thenReturn(Optional.of(source));

        // When perform get

        Optional<JsonNode> subscription = service.get(email);

        // Then check subscription

        assertThat(subscription).hasValue(source);

    }

}
