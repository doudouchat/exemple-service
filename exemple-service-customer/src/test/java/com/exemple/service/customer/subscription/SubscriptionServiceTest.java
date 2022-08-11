package com.exemple.service.customer.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringJUnitConfig(CustomerTestConfiguration.class)
class SubscriptionServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    @Test
    void save() {

        // Given email

        String email = "jean@gmail.com";

        // When perform save

        JsonNode source = MAPPER.createObjectNode();

        service.save(email, source);

        // Then check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).put("subscription_date", ServiceContextExecution.context().getDate().toString());

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue()).isEqualTo(expectedSubscription);

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("subscription"), Mockito.eq(EventType.CREATE));
        assertThat(eventCaptor.getValue()).isEqualTo(expectedSubscription);

    }

    @Test
    void update() throws IOException {

        // Given email

        String email = "jean@gmail.com";

        // When perform save

        JsonNode source = MAPPER.createObjectNode();
        JsonNode previousSource = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\"}");

        service.save(email, source, previousSource);

        // Then check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).put("subscription_date", ServiceContextExecution.context().getDate().toString());

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(subscriptionCaptor.capture(), Mockito.eq(previousSource));
        assertThat(subscriptionCaptor.getValue()).isEqualTo(expectedSubscription);

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("subscription"), Mockito.eq(EventType.UPDATE));
        assertThat(eventCaptor.getValue()).isEqualTo(expectedSubscription);

    }

    @DisplayName("get subscription")
    @Test
    void get() throws IOException {

        // Given email

        String email = "jean@gmail.com";

        // And mock resource

        JsonNode source = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\"}");

        Mockito.when(resource.get(email)).thenReturn(Optional.of(source));

        // When perform get

        Optional<JsonNode> subscription = service.get(email);

        // Then check subscription

        assertThat(subscription).hasValue(source);

    }

}
