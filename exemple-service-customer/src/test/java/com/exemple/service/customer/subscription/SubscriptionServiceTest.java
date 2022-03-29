package com.exemple.service.customer.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
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
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringJUnitConfig(CustomerTestConfiguration.class)
public class SubscriptionServiceTest {

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

    private static Stream<Arguments> save() throws IOException {

        JsonNode source = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\"}");

        return Stream.of(
                Arguments.of(Optional.empty(), true),
                Arguments.of(Optional.of(source), false));
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

        JsonNode source = MAPPER.createObjectNode();

        boolean created = service.save(email, source);

        // Then check subscription

        assertThat(created).isEqualTo(expectedCreated);

        // And check save resource

        JsonNode expectedSubscription = ((ObjectNode) source).put("subscription_date", ServiceContextExecution.context().getDate().toString());

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue()).isEqualTo(expectedSubscription);

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("subscription"), Mockito.eq(EventType.CREATE));
        assertThat(eventCaptor.getValue()).isEqualTo(expectedSubscription);

    }

    @DisplayName("get subscription")
    @Test
    public void get() throws IOException {

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
