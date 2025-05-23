package com.exemple.service.resource.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.AccountContextExecution;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.account.event.AccountEventResource;
import com.exemple.service.resource.account.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.account.model.AccountEvent;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.history.ExpectedHistory;
import com.exemple.service.resource.common.history.HistoryAssert;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.experimental.SuperBuilder;

@TestMethodOrder(OrderAnnotation.class)
@SpringBootTest(classes = ResourceTestConfiguration.class)
@ActiveProfiles("test")
class AccountResourceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountResource resource;

    @Autowired
    private AccountHistoryResource accountHistoryResource;

    @Autowired
    private AccountEventResource accountEventResource;

    @Autowired
    private CqlSession session;

    @BeforeEach
    void initExecutionContextDate() {

        OffsetDateTime now = OffsetDateTime.now();
        ServiceContextExecution.setDate(now);
        ServiceContextExecution.setPrincipal(() -> "user");
        ServiceContextExecution.setApp("test");
        ServiceContextExecution.setVersion("v1");

    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveField {

        private final UUID id = UUID.randomUUID();

        private OffsetDateTime createDate;

        @BeforeAll
        void deleteAccount() {

            resource.removeByUsername("email", "jean.dupond@gmail");

        }

        @BeforeAll
        void initAccountContext() {

            AccountContextExecution.setPreviousAccount(MAPPER.nullNode());

        }

        @AfterEach
        void updateAccountContext() {

            AccountContextExecution.setPreviousAccount(resource.get(id).get());

        }

        @Test
        @DisplayName("save email")
        @Order(1)
        void save() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupond@gmail"}"

                    """.formatted(id));

            // When perform save
            resource.create(account);

            // Then check history
            createDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/email")
                    .date(createDate)
                    .value("jean.dupond@gmail")
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupond@gmail"}"

                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.CREATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupond@gmail"}"

                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("update email and add age")
        @Order(2)
        void update() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupont@gmail", "age": 19}"

                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/email")
                    .date(updateDate)
                    .value("jean.dupont@gmail")
                    .previousValue("jean.dupond@gmail")
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/age")
                    .date(updateDate)
                    .value(19)
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupont@gmail", "age": 19}"

                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupont@gmail", "age": 19}"

                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("remove email and age")
        @Order(3)
        void remove() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": null, "age": null}"

                    """.formatted(id));
            AccountContextExecution.setPreviousAccount(resource.get(id).get());

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/email")
                    .date(updateDate)
                    .previousValue("jean.dupont@gmail")
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/age")
                    .date(updateDate)
                    .previousValue(19)
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s"}"

                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s"}"

                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(3);
        }

    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveMap {

        private final UUID id = UUID.randomUUID();

        private OffsetDateTime createDate;

        @AfterEach
        void updateAccountContext() {

            AccountContextExecution.setPreviousAccount(resource.get(id).get());

        }

        @BeforeAll
        void initAccountContext() {

            AccountContextExecution.setPreviousAccount(MAPPER.nullNode());

        }

        @Test
        @DisplayName("save addresses")
        @Order(1)
        void save() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}"

                    """.formatted(id));

            // When perform save
            resource.create(account);

            // Then check history
            createDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/floor")
                    .date(createDate)
                    .value(5)
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/street")
                    .date(createDate)
                    .value("1 rue de la poste")
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}"

                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.CREATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}"

                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(1);
        }

        @Test
        @DisplayName("add 1 addresse")
        @Order(2)
        void addAdresse() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                     {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "1 rue de la paris"}}}

                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/floor")
                    .date(createDate)
                    .value(5)
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/street")
                    .date(createDate)
                    .value("1 rue de la poste")
                    .build();

            var expectedHistory4 = ExpectedAccountHistory.builder()
                    .field("/addresses/job/street")
                    .date(updateDate)
                    .value("1 rue de la paris")
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3, expectedHistory4));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "1 rue de la paris"}}}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "1 rue de la paris"}}}

                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("update addresse")
        @Order(3)
        void updateAddresse() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {
                    "id":  "%s",
                    "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "10 rue de la paris", "floor": 5}}
                    }
                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/floor")
                    .date(createDate)
                    .value(5)
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/street")
                    .date(createDate)
                    .value("1 rue de la poste")
                    .build();

            var expectedHistory4 = ExpectedAccountHistory.builder()
                    .field("/addresses/job/street")
                    .date(updateDate)
                    .value("10 rue de la paris")
                    .previousValue("1 rue de la paris")
                    .build();

            var expectedHistory5 = ExpectedAccountHistory.builder()
                    .field("/addresses/job/floor")
                    .date(updateDate)
                    .value(5)
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3, expectedHistory4, expectedHistory5));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {
                    "id": "%s",
                    "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "10 rue de la paris", "floor": 5}}}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "10 rue de la paris", "floor": 5}}}
                    """
                            .formatted(
                                    id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(3);

        }

        @Test
        @DisplayName("delete addresse")
        @Order(4)
        void deleteAddresse() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": null}}
                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/floor")
                    .date(createDate)
                    .value(5)
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/addresses/home/street")
                    .date(createDate)
                    .value("1 rue de la poste")
                    .build();

            var expectedHistory4 = ExpectedAccountHistory.builder()
                    .field("/addresses/job")
                    .date(updateDate)
                    .previousValue(MAPPER.readTree("""
                                                   {"street": "10 rue de la paris", "floor": 5}
                                                   """))
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3, expectedHistory4));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}
                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(4);
        }

        @Test
        @DisplayName("remove all addresses")
        @Order(5)
        void remove() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": null}
                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/addresses")
                    .date(updateDate)
                    .previousValue(MAPPER.readTree("""
                                                   {"home": {"street": "1 rue de la poste", "floor": 5}}
                                                   """))
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(5);
        }
    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveSet {

        private final UUID id = UUID.randomUUID();

        private OffsetDateTime createDate;

        @AfterEach
        void updateAccountContext() {

            AccountContextExecution.setPreviousAccount(resource.get(id).get());

        }

        @BeforeAll
        void initAccountContext() {

            AccountContextExecution.setPreviousAccount(MAPPER.nullNode());

        }

        @Test
        @DisplayName("save cgus")
        @Order(1)
        void save() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}]}
                    """.formatted(id));

            // When perform save
            resource.create(account);

            // Then check history
            createDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/cgus/0/code")
                    .date(createDate)
                    .value("code_1")
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/cgus/0/version")
                    .date(createDate)
                    .value("v1")
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}]}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.CREATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}]}
                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(1);

        }

        @Test
        @DisplayName("add 1 cgu")
        @Order(2)
        void addCgu() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}, {"code": "code_1", "version": "v2"}]}
                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/cgus/0/code")
                    .date(createDate)
                    .value("code_1")
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            var expectedHistory3 = ExpectedAccountHistory.builder()
                    .field("/cgus/0/version")
                    .date(createDate)
                    .value("v1")
                    .build();

            var expectedHistory4 = ExpectedAccountHistory.builder()
                    .field("/cgus/1/code")
                    .date(updateDate)
                    .value("code_1")
                    .build();

            var expectedHistory5 = ExpectedAccountHistory.builder()
                    .field("/cgus/1/version")
                    .date(updateDate)
                    .value("v2")
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2, expectedHistory3, expectedHistory4, expectedHistory5));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}, {"code": "code_1", "version": "v2"}]}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                     {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}, {"code": "code_1", "version": "v2"}]}
                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("remove all cgus")
        @Order(3)
        void remove() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": null}
                    """.formatted(id));

            // When perform save
            resource.update(account);

            // Then check history
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();

            var expectedHistory1 = ExpectedAccountHistory.builder()
                    .field("/cgus")
                    .date(updateDate)
                    .previousValue(MAPPER.readTree("""
                                                   [{"code": "code_1", "version": "v1"}, {"code": "code_1", "version": "v2"}]
                                                   """))
                    .build();

            var expectedHistory2 = ExpectedAccountHistory.builder()
                    .field("/id")
                    .date(createDate)
                    .value(id.toString())
                    .build();

            new AccountHistoryAssert(id).contains(List.of(expectedHistory1, expectedHistory2));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            var expectedEvent = new AccountEvent();
            expectedEvent.setEventType(EventType.UPDATE);
            expectedEvent.setDate(ServiceContextExecution.context().getDate().toInstant().truncatedTo(ChronoUnit.MILLIS));
            expectedEvent.setApplication("test");
            expectedEvent.setVersion("v1");
            expectedEvent.setUser("user");
            expectedEvent.setId(id);
            expectedEvent.setData((MAPPER.readTree(
                    """
                     {"id": "%s"}
                    """.formatted(id))));
            assertThat(event).usingRecursiveComparison()
                    .isEqualTo(expectedEvent);

            var events = session
                    .execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());
            assertThat(events.all()).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    @Test
    void getNotExist() {

        // When perform get
        Optional<JsonNode> account = resource.get(UUID.randomUUID());

        // And check account
        assertThat(account).isEmpty();
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class AccountUsername {

        private UUID id = UUID.randomUUID();

        private String email;

        @BeforeEach
        void saveAccount() throws IOException {
            email = UUID.randomUUID() + "@gmail";
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            resource.create(account);

        }

        @Test
        void saveUnique() throws IOException {

            // When perform
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            Throwable throwable = catchThrowable(() -> resource.create(account));

            // Then check throwable
            assertThat(throwable).isInstanceOf(UsernameAlreadyExistsException.class).hasFieldOrPropertyWithValue("username", email);

            // And check account
            assertThat(resource.getIdByUsername("email", email)).hasValue(this.id);
        }

        @Test
        void updateUnique() throws IOException {

            // When perform
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            JsonNode previousAccount = MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id));
            AccountContextExecution.setPreviousAccount(previousAccount);
            Throwable throwable = catchThrowable(() -> resource.update(account));

            // Then check throwable
            assertThat(throwable).isInstanceOf(UsernameAlreadyExistsException.class).hasFieldOrPropertyWithValue("username", email);

            // And check account
            assertThat(resource.getIdByUsername("email", email)).hasValue(this.id);
        }

        @Test
        void updateSuccessIfEmailNotChange() throws IOException {

            // When perform
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            JsonNode previousAccount = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            AccountContextExecution.setPreviousAccount(previousAccount);
            Throwable throwable = catchThrowable(() -> resource.update(account));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();

            // And check account
            assertThat(resource.getIdByUsername("email", email)).hasValue(this.id);
        }

        @Test
        void updateSuccessIfEmailChange() throws IOException {

            // setup email
            var newEmail = UUID.randomUUID() + "@gmail";

            // When perform
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, newEmail));
            JsonNode previousAccount = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            AccountContextExecution.setPreviousAccount(previousAccount);
            Throwable throwable = catchThrowable(() -> resource.update(account));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();

            // And check account
            assertThat(resource.getIdByUsername("email", newEmail)).hasValue(this.id);
        }

        @Test
        void updateSuccessIfEmailIsRemove() throws IOException {

            // When perform
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id));
            JsonNode previousAccount = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            AccountContextExecution.setPreviousAccount(previousAccount);
            Throwable throwable = catchThrowable(() -> resource.update(account));

            // Then check none exception
            assertThat(throwable).as("None exception is expected").isNull();

            // And check account
            assertThat(resource.getIdByUsername("email", email)).isEmpty();
        }

        @Test
        void removeByUsername() {

            // When perform
            resource.removeByUsername("email", email);

            // And check account
            assertThat(resource.getIdByUsername("email", email)).isEmpty();
        }

        @DisplayName("multiple save")
        @Test
        void multipleSave() throws InterruptedException {

            // setup email
            var uniqueEmail = UUID.randomUUID() + "@gmail";

            // when perform multiple update

            List<Throwable> exceptions = new ArrayList<>();
            try (ExecutorService executorService = new ThreadPoolExecutor(5, 100, 1000, TimeUnit.SECONDS, new LinkedBlockingQueue<>())) {

                for (int i = 0; i < 10; i++) {
                    executorService.submit(() -> exceptions.add(save(uniqueEmail)));
                }

                executorService.awaitTermination(5, TimeUnit.SECONDS);
                executorService.shutdown();
            }

            // And check exceptions
            assertThat(exceptions.stream().filter(UsernameAlreadyExistsException.class::isInstance)).hasSize(9);

        }

        private Throwable save(String email) throws IOException {

            OffsetDateTime now = OffsetDateTime.now();
            ServiceContextExecution.setDate(now);
            ServiceContextExecution.setPrincipal(() -> "user");
            ServiceContextExecution.setApp("test");
            ServiceContextExecution.setVersion("v1");

            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            JsonNode previousAccount = MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id));
            AccountContextExecution.setPreviousAccount(previousAccount);

            return catchThrowable(() -> resource.update(account));
        }
    }

    public class AccountHistoryAssert extends HistoryAssert<UUID> {

        public AccountHistoryAssert(UUID id) {
            super(id, accountHistoryResource.findById(id));
        }
    }

    @SuperBuilder
    public static class ExpectedAccountHistory extends ExpectedHistory<UUID> {

        @Override
        public AccountHistory buildHistory(UUID id) {

            var history = new AccountHistory();
            history.setId(id);
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
