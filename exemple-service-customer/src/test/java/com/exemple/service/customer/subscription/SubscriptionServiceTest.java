package com.exemple.service.customer.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExtension;
import com.exemple.service.context.WithServiceContext;
import com.exemple.service.customer.core.CustomerTestConfiguration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

@SpringJUnitConfig(CustomerTestConfiguration.class)
@ExtendWith(ServiceContextExtension.class)
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

    @Test
    @WithServiceContext
    void create() {

        // Given email

        String email = "jean@gmail.com";

        // When perform save

        JsonNode source = MAPPER.createObjectNode();

        service.create(email, source);

        // Then check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).set("subscription_date",
                StringNode.valueOf(ServiceContext.SERVICE_CONTEXT.get().date().toString()));

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).create(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue()).isEqualTo(expectedSubscription);

    }

    @Test
    @WithServiceContext
    void update() {

        // Given email

        String email = "jean@gmail.com";

        // When perform save

        JsonNode source = MAPPER.createObjectNode();

        service.update(email, source);

        // Then check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).set("subscription_date",
                StringNode.valueOf(ServiceContext.SERVICE_CONTEXT.get().date().toString()));

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
