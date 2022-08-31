package com.exemple.service.resource.subscription;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
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
import com.exemple.service.resource.subscription.history.SubscriptionHistoryResource;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;
import com.exemple.service.resource.subscription.model.SubscriptionHistory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@TestMethodOrder(OrderAnnotation.class)
@SpringJUnitConfig(ResourceTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscriptionResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private SubscriptionEventResource subscriptionEventResource;

    @Autowired
    private SubscriptionHistoryResource subscriptionHistoryResource;

    @Autowired
    private CqlSession session;

    private final String email = UUID.randomUUID() + "@gmail.com";

    @BeforeEach
    void initExecutionContextDate() {

        OffsetDateTime now = OffsetDateTime.now();
        ServiceContextExecution.setDate(now);
        ServiceContextExecution.setPrincipal(() -> "user");
        ServiceContextExecution.setApp("test");
        ServiceContextExecution.setVersion("v1");

    }

    @Test
    @Order(0)
    void save() throws IOException {

        // Given build subscription
        JsonNode subscription = MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01T09:00:00Z"}
                """.formatted(email));

        // When perform save
        resource.save(subscription);

        // Then check subscription
        Optional<JsonNode> result = resource.get(email);
        assertThat(result).hasValue(MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01 09:00:00.000Z"}
                """.formatted(email)));

        // And check event
        OffsetDateTime createDate = ServiceContextExecution.context().getDate();
        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        assertAll(
                () -> assertThat(event.getEventType()).isEqualTo(EventType.CREATE),
                () -> assertThat(event.getLocalDate()).isEqualTo(createDate.toLocalDate()),
                () -> assertThat(event.getData().get("email").textValue()).isEqualTo(email),
                () -> assertThat(event.getData().get("update_date").textValue()).isEqualTo("2019-01-01T09:00:00Z"));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all()).hasSize(1);

        // and check history
        List<SubscriptionHistory> histories = subscriptionHistoryResource.findById(email);
        assertAll(
                () -> assertThat(histories).hasSize(2),
                () -> assertHistory(subscriptionHistoryResource.findByIdAndField(email, "/email"), email, createDate.toInstant()),
                () -> assertHistory(subscriptionHistoryResource.findByIdAndField(email, "/update_date"), "2019-01-01T09:00:00Z",
                        createDate.toInstant()));

    }

    @Test
    @Order(1)
    void update() throws IOException {

        // Given build subscription
        JsonNode subscription = MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01T10:00:00Z"}
                """.formatted(email));
        JsonNode previouSubscription = resource.get(email).get();

        // When perform save
        resource.save(subscription, previouSubscription);

        // Then check subscription
        Optional<JsonNode> result = resource.get(email);
        assertThat(result).hasValue(MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01 10:00:00.000Z"}
                """.formatted(email)));

        // And check event
        OffsetDateTime createDate = ServiceContextExecution.context().getDate();
        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        assertAll(
                () -> assertThat(event.getEventType()).isEqualTo(EventType.UPDATE),
                () -> assertThat(event.getLocalDate()).isEqualTo(createDate.toLocalDate()),
                () -> assertThat(event.getData().get("email").textValue()).isEqualTo(email),
                () -> assertThat(event.getData().get("update_date").textValue()).isEqualTo("2019-01-01T10:00:00Z"));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all()).hasSize(2);

        // and check history
        List<SubscriptionHistory> histories = subscriptionHistoryResource.findById(email);
        assertAll(
                () -> assertThat(histories).hasSize(2),
                () -> assertHistory(subscriptionHistoryResource.findByIdAndField(email, "/email"), email),
                () -> assertHistory(subscriptionHistoryResource.findByIdAndField(email, "/update_date"), "2019-01-01T10:00:00Z",
                        createDate.toInstant()));

    }

    @Test
    @Order(2)
    void delete() {

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
        assertThat(countAccountEvents.all()).hasSize(3);

    }

    private static void assertHistory(SubscriptionHistory subscriptionHistory, Object expectedValue, Instant expectedDate) {

        assertAll(
                () -> assertThat(subscriptionHistory.getValue().asText()).isEqualTo(expectedValue.toString()),
                () -> assertThat(subscriptionHistory.getDate()).isCloseTo(expectedDate, Assertions.within(1, ChronoUnit.MILLIS)),
                () -> assertHistory(subscriptionHistory));

    }

    private static void assertHistory(SubscriptionHistory subscriptionHistory, Object expectedValue) {

        assertAll(
                () -> assertThat(subscriptionHistory.getValue().asText()).isEqualTo(expectedValue.toString()),
                () -> assertHistory(subscriptionHistory));

    }

    private static void assertHistory(SubscriptionHistory subscriptionHistory) {

        assertAll(
                () -> assertThat(subscriptionHistory.getApplication()).isEqualTo("test"),
                () -> assertThat(subscriptionHistory.getVersion()).isEqualTo("v1"),
                () -> assertThat(subscriptionHistory.getUser()).isEqualTo("user"));

    }

}
