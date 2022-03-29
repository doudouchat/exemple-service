package com.exemple.service.resource.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.subscription.SubscriptionResource;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.subscription.event.SubscriptionEventResource;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestMethodOrder(OrderAnnotation.class)
@SpringJUnitConfig(ResourceTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubscriptionResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private SubscriptionEventResource subscriptionEventResource;

    @Autowired
    private CqlSession session;

    private final String email = UUID.randomUUID() + "@gmail.com";

    @BeforeEach
    public void initExecutionContextDate() {

        OffsetDateTime now = OffsetDateTime.now();
        ServiceContextExecution.context().setDate(now);
        ServiceContextExecution.context().setPrincipal(() -> "user");
        ServiceContextExecution.context().setApp("test");
        ServiceContextExecution.context().setVersion("v1");

    }

    @Test
    @Order(0)
    public void save() throws IOException {

        // Given build account
        JsonNode subscription = MAPPER.readTree("{\"email\": \"" + email + "\"}");

        // When perform save
        resource.save(subscription);

        // Then check subscription
        Optional<JsonNode> result = resource.get(email);
        assertThat(result).hasValue(MAPPER.readTree("{\"email\": \"" + email + "\"}"));

        // And check event
        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        assertAll(
                () -> assertThat(event.getEventType()).isEqualTo(EventType.CREATE),
                () -> assertThat(event.getLocalDate()).isEqualTo(ServiceContextExecution.context().getDate().toLocalDate()),
                () -> assertThat(event.getData().get("email").textValue()).isEqualTo(email));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all()).hasSize(1);

    }

    @Test
    @Order(1)
    public void delete() {

        // When perform delete
        resource.delete(email);

        // Then check subscription
        assertThat(resource.get(email)).isEmpty();

        // And check event
        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        assertThat(event.getEventType()).isEqualTo(EventType.DELETE);
        assertThat(event.getLocalDate()).isEqualTo(ServiceContextExecution.context().getDate().toLocalDate());
        assertThat(event.getData()).isNull();

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all()).hasSize(2);

    }

}
