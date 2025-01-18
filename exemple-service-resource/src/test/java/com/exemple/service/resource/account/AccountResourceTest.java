package com.exemple.service.resource.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.Instant;
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

import org.assertj.core.api.Assertions;
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
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.account.event.AccountEventResource;
import com.exemple.service.resource.account.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.account.model.AccountEvent;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

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
            resource.save(account);

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            createDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(2),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), "jean.dupond@gmail", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupond@gmail"}"

                    """.formatted(id)));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            assertAll(
                    () -> assertThat(event.getEventType()).isEqualTo(EventType.CREATE),
                    () -> assertThat(event.getLocalDate()).isEqualTo(ServiceContextExecution.context().getDate().toLocalDate()),
                    () -> assertThat(event.getData()).isNotNull());

            ResultSet acountAccountEvents = session.execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("local_date")
                    .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
            assertThat(acountAccountEvents.all()).hasSizeGreaterThanOrEqualTo(1);
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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(3),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), "jean.dupont@gmail", updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/age"), 19, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "email": "jean.dupont@gmail", "age": 19}"

                    """.formatted(id)));

            // And check event
            ResultSet acountAccountEvents = session.execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("local_date")
                    .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
            assertThat(acountAccountEvents.all()).hasSizeGreaterThanOrEqualTo(2);
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

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(3),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/age"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s"}"

                    """.formatted(id)));
        }

    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveMap {

        private final UUID id = UUID.randomUUID();

        private OffsetDateTime createDate;

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
            resource.save(account);

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            createDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(3),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}"

                    """.formatted(id)));
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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(4),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/street"), "1 rue de la paris",
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "1 rue de la paris"}}}
                    """.formatted(id)));
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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(5),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/street"), "10 rue de la paris",
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/floor"), 5,
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {
                    "id": "%s",
                    "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}, "job": {"street": "10 rue de la paris", "floor": 5}}}
                    """.formatted(id)));

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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(4),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job"), JsonNodeType.NULL,
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "addresses": {"home": {"street": "1 rue de la poste", "floor": 5}}}
                    """.formatted(id)));
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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(2),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id)));
        }
    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveSet {

        private final UUID id = UUID.randomUUID();

        private OffsetDateTime createDate;

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
            resource.save(account);

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            createDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(3),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/code"), "code_1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/version"), "v1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}]}
                    """.formatted(id)));

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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(5),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/code"), "code_1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/version"), "v1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/1/code"), "code_1", updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/1/version"), "v2", updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s", "cgus": [{"code": "code_1", "version": "v1"}, {"code": "code_1", "version": "v2"}]}
                    """.formatted(id)));
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
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories).hasSize(2),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            Optional<JsonNode> result = resource.get(id);
            assertThat(result).hasValue(MAPPER.readTree(
                    """
                    {"id": "%s"}
                    """.formatted(id)));
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
            resource.save(account);

        }

        @Test
        void saveUnique() throws IOException {

            // When perform
            JsonNode account = MAPPER.readTree(
                    """
                    {"id": "%s", "email": "%s"}
                    """.formatted(id, email));
            Throwable throwable = catchThrowable(() -> resource.save(account));

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
            Throwable throwable = catchThrowable(() -> resource.save(account, previousAccount));

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
            Throwable throwable = catchThrowable(() -> resource.save(account, previousAccount));

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
            Throwable throwable = catchThrowable(() -> resource.save(account, previousAccount));

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
            Throwable throwable = catchThrowable(() -> resource.save(account, previousAccount));

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

            return catchThrowable(() -> resource.save(account, previousAccount));
        }
    }

    private static void assertHistory(AccountHistory accountHistory, JsonNodeType expectedJsonNodeType, Instant expectedDate) {

        assertAll(
                () -> assertThat(accountHistory.getValue().getNodeType()).isEqualTo(expectedJsonNodeType),
                () -> assertThat(accountHistory.getDate()).isCloseTo(expectedDate, Assertions.within(1, ChronoUnit.MILLIS)),
                () -> assertHistory(accountHistory));

    }

    private static void assertHistory(AccountHistory accountHistory, Object expectedValue, Instant expectedDate) {

        assertAll(
                () -> assertThat(accountHistory.getValue().asText()).isEqualTo(expectedValue.toString()),
                () -> assertThat(accountHistory.getDate()).isCloseTo(expectedDate, Assertions.within(1, ChronoUnit.MILLIS)),
                () -> assertHistory(accountHistory));

    }

    private static void assertHistory(AccountHistory accountHistory) {

        assertAll(
                () -> assertThat(accountHistory.getApplication()).isEqualTo("test"),
                () -> assertThat(accountHistory.getVersion()).isEqualTo("v1"),
                () -> assertThat(accountHistory.getUser()).isEqualTo("user"));

    }
}
