package com.exemple.service.resource.subscription;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.context.SubscriptionContextExecution;
import com.exemple.service.customer.subscription.SubscriptionResource;
import com.exemple.service.resource.common.history.ExpectedHistory;
import com.exemple.service.resource.common.history.HistoryAssert;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.subscription.event.SubscriptionEventResource;
import com.exemple.service.resource.subscription.history.SubscriptionHistoryResource;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;
import com.exemple.service.resource.subscription.model.SubscriptionHistory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.SuperBuilder;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(classes = ResourceTestConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
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

    private OffsetDateTime createDate;

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
        resource.create(subscription);

        // Then check subscription
        Optional<JsonNode> result = resource.get(email);
        assertThat(result).hasValue(MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01 09:00:00.000Z"}
                """.formatted(email)));

        // And check event
        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        var expectedEvent = new SubscriptionEvent();
        expectedEvent.setEventType(EventType.CREATE);
        expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
        expectedEvent.setApplication("test");
        expectedEvent.setVersion("v1");
        expectedEvent.setUser("user");
        expectedEvent.setEmail(email);
        expectedEvent.setData((MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01T09:00:00Z"}

                """.formatted(email))));
        assertThat(event).usingRecursiveComparison()
                .isEqualTo(expectedEvent);

        var events = session.execute(
                QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("email").isEqualTo(QueryBuilder.literal(email)).build());
        assertThat(events.all()).hasSize(1);

        // and check history
        createDate = ServiceContextExecution.context().getDate();

        var expectedHistory1 = ExpectedSubscriptionHistory.builder()
                .field("/email")
                .date(createDate)
                .value(email)
                .build();

        var expectedHistory2 = ExpectedSubscriptionHistory.builder()
                .field("/update_date")
                .date(createDate)
                .value("2019-01-01T09:00:00Z")
                .build();

        new SubscriptionHistoryAssert(email).contains(List.of(expectedHistory1, expectedHistory2));

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
        SubscriptionContextExecution.setPreviousSubscription(previouSubscription);

        // When perform save
        resource.update(subscription);

        // Then check subscription
        Optional<JsonNode> result = resource.get(email);
        assertThat(result).hasValue(MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01 10:00:00.000Z"}
                """.formatted(email)));

        // And check event
        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        var expectedEvent = new SubscriptionEvent();
        expectedEvent.setEventType(EventType.UPDATE);
        expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
        expectedEvent.setApplication("test");
        expectedEvent.setVersion("v1");
        expectedEvent.setUser("user");
        expectedEvent.setEmail(email);
        expectedEvent.setData((MAPPER.readTree(
                """
                {"email": "%s", "update_date": "2019-01-01T10:00:00Z"}

                """.formatted(email))));
        assertThat(event).usingRecursiveComparison()
                .isEqualTo(expectedEvent);

        var events = session.execute(
                QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("email").isEqualTo(QueryBuilder.literal(email)).build());
        assertThat(events.all()).hasSize(2);

        // and check history
        var updateDate = ServiceContextExecution.context().getDate();

        var expectedHistory1 = ExpectedSubscriptionHistory.builder()
                .field("/email")
                .date(createDate)
                .value(email)
                .build();

        var expectedHistory2 = ExpectedSubscriptionHistory.builder()
                .field("/update_date")
                .date(updateDate)
                .previousValue("2019-01-01T09:00:00Z")
                .value("2019-01-01T10:00:00Z")
                .build();

        new SubscriptionHistoryAssert(email).contains(List.of(expectedHistory1, expectedHistory2));

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
        var expectedEvent = new SubscriptionEvent();
        expectedEvent.setEventType(EventType.DELETE);
        expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
        expectedEvent.setApplication("test");
        expectedEvent.setVersion("v1");
        expectedEvent.setUser("user");
        expectedEvent.setEmail(email);
        assertThat(event).usingRecursiveComparison()
                .isEqualTo(expectedEvent);

        var events = session.execute(
                QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("email").isEqualTo(QueryBuilder.literal(email)).build());
        assertThat(events.all()).hasSize(3);

    }

    public class SubscriptionHistoryAssert extends HistoryAssert<String> {

        public SubscriptionHistoryAssert(String email) {
            super(email, subscriptionHistoryResource.findById(email));
        }
    }

    @SuperBuilder
    public static class ExpectedSubscriptionHistory extends ExpectedHistory<String> {

        @Override
        public SubscriptionHistory buildHistory(String email) {

            var history = new SubscriptionHistory();
            history.setId(email);
            history.setField(getField());
            history.setDate(getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            history.setApplication("test");
            history.setVersion("v1");
            history.setUser("user");
            history.setPreviousValue(getPreviousValue());
            history.setValue(getValue());

            return history;
        }

    }

}
