package com.exemple.service.resource.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
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
import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.account.event.AccountEventResource;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.account.model.AccountEvent;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@TestMethodOrder(OrderAnnotation.class)
@SpringJUnitConfig(ResourceTestConfiguration.class)
public class AccountResourceTest {

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
    public void initExecutionContextDate() {

        OffsetDateTime now = OffsetDateTime.now();
        ServiceContextExecution.context().setDate(now);
        ServiceContextExecution.context().setPrincipal(() -> "user");
        ServiceContextExecution.context().setApp("test");
        ServiceContextExecution.context().setVersion("v1");

    }

    @Nested
    @TestMethodOrder(OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class saveField {

        private final UUID id = UUID.randomUUID();

        private OffsetDateTime createDate;

        @Test
        @DisplayName("save email")
        @Order(1)
        public void save() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"email\": \"jean.dupond@gmail\"}");

            // When perform save
            resource.save(account);

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            createDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(2))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), "jean.dupond@gmail", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER.readTree("{\"id\": \"" + id + "\", \"email\": \"jean.dupond@gmail\"}")));

            // And check event
            AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
            assertAll(
                    () -> assertThat(event.getEventType(), is(EventType.CREATE)),
                    () -> assertThat(event.getLocalDate(), is(ServiceContextExecution.context().getDate().toLocalDate())),
                    () -> assertThat(event.getData(), is(notNullValue())));

            ResultSet acountAccountEvents = session.execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("local_date")
                    .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
            assertThat(acountAccountEvents.all().size(), greaterThanOrEqualTo(1));
        }

        @Test
        @DisplayName("update email and add age")
        @Order(2)
        public void update() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"email\": \"jean.dupont@gmail\", \"age\": 19}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(3))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), "jean.dupont@gmail", updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/age"), 19, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER.readTree("{\"id\": \"" + id + "\", \"email\": \"jean.dupont@gmail\", \"age\": 19}")));

            // And check event
            ResultSet acountAccountEvents = session.execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("local_date")
                    .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
            assertThat(acountAccountEvents.all().size(), greaterThanOrEqualTo(2));
        }

        @Test
        @DisplayName("remove email and age")
        @Order(3)
        public void remove() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"email\": null, \"age\": null}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(3))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/age"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER.readTree("{\"id\": \"" + id + "\"}")));
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
        public void save() throws IOException {

            // Given build account
            JsonNode account = MAPPER
                    .readTree("{\"id\": \"" + id + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}}}");

            // When perform save
            resource.save(account);

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            createDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(3))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result,
                    is(MAPPER.readTree("{\"id\": \"" + id + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}}}")));
        }

        @Test
        @DisplayName("add 1 addresse")
        @Order(2)
        public void addAdresse() throws IOException {

            // Given build account
            JsonNode account = MAPPER
                    .readTree("{\"id\": \"" + id
                            + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}, \"job\": {\"street\": \"1 rue de la paris\"}}}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(4))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/street"), "1 rue de la paris",
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER
                    .readTree("{\"id\": \"" + id
                            + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}, \"job\": {\"street\": \"1 rue de la paris\"}}}")));
        }

        @Test
        @DisplayName("update addresse")
        @Order(3)
        public void updateAddresse() throws IOException {

            // Given build account
            JsonNode account = MAPPER
                    .readTree("{\"id\": \"" + id
                            + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}, \"job\": {\"street\": \"10 rue de la paris\", \"floor\": 5}}}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(5))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/street"), "10 rue de la paris",
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/floor"), 5,
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER
                    .readTree("{\"id\": \"" + id
                            + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}, \"job\": {\"street\": \"10 rue de la paris\", \"floor\": 5}}}")));
        }

        @Test
        @DisplayName("delete addresse")
        @Order(4)
        public void deleteAddresse() throws IOException {

            // Given build account
            JsonNode account = MAPPER
                    .readTree(
                            "{\"id\": \"" + id + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}, \"job\": null}}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(4))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                            createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), 5, createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job"), JsonNodeType.NULL,
                            updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER
                    .readTree("{\"id\": \"" + id + "\", \"addresses\": {\"home\": {\"street\": \"1 rue de la poste\", \"floor\": 5}}}")));
        }

        @Test
        @DisplayName("remove all addresses")
        @Order(5)
        public void remove() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"addresses\": null}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(2))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER.readTree("{\"id\": \"" + id + "\"}")));
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
        public void save() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"cgus\": [{\"code\": \"code_1\", \"version\": \"v1\"}]}");

            // When perform save
            resource.save(account);

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            createDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(3))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/code"), "code_1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/version"), "v1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER.readTree("{\"id\": \"" + id + "\", \"cgus\": [{\"code\": \"code_1\", \"version\": \"v1\"}]}")));
        }

        @Test
        @DisplayName("add 1 cgu")
        @Order(2)
        public void addCgu() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree(
                    "{\"id\": \"" + id + "\", \"cgus\": [{\"code\": \"code_1\", \"version\": \"v1\"}, {\"code\": \"code_1\", \"version\": \"v2\"}]}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(5))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/code"), "code_1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/version"), "v1", createDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/1/code"), "code_1", updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/1/version"), "v2", updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER
                    .readTree("{\"id\": \"" + id
                            + "\", \"cgus\": [{\"code\": \"code_1\", \"version\": \"v1\"}, {\"code\": \"code_1\", \"version\": \"v2\"}]}")));
        }

        @Test
        @DisplayName("remove all cgus")
        @Order(3)
        public void remove() throws IOException {

            // Given build account
            JsonNode account = MAPPER.readTree("{\"id\": \"" + id + "\", \"cgus\": null}");

            // When perform save
            resource.save(account, resource.get(id).get());

            // Then check history
            List<AccountHistory> histories = accountHistoryResource.findById(id);
            OffsetDateTime updateDate = ServiceContextExecution.context().getDate();
            assertAll(
                    () -> assertThat(histories, is(hasSize(2))),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus"), JsonNodeType.NULL, updateDate.toInstant()),
                    () -> assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id, createDate.toInstant()));

            // And check account
            JsonNode result = resource.get(id).get();
            assertThat(result, is(MAPPER.readTree("{\"id\": \"" + id + "\"}")));
        }
    }

    @Test
    public void getNotExist() {

        // When perform get
        Optional<JsonNode> account = resource.get(UUID.randomUUID());

        // And check account
        assertThat(account.isPresent(), is(false));
    }

    @Test
    public void findByIndex() throws IOException {

        // Given build accounts
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        JsonNode account1 = MAPPER.readTree("{\"id\": \"" + id1 + "\", \"status\": \"NEW\"}");
        JsonNode account2 = MAPPER.readTree("{\"id\": \"" + id2 + "\", \"status\": \"NEW\"}");
        JsonNode account3 = MAPPER.readTree("{\"id\": \"" + id3 + "\", \"status\": \"OLD\"}");

        resource.save(account1);
        resource.save(account2);
        resource.save(account3);

        // When perform
        Set<JsonNode> accounts = resource.findByIndex("status", "NEW");

        // And check accounts

        List<String> ids = accounts.stream().map(account -> account.get(AccountField.ID.field).asText()).collect(Collectors.toList());

        assertAll(
                () -> assertThat(ids, hasSize(2)),
                () -> assertThat(ids, containsInAnyOrder(id1.toString(), id2.toString())),
                () -> assertThat(ids, not(containsInAnyOrder(id3.toString()))));
    }

    private static void assertHistory(AccountHistory accountHistory, JsonNodeType expectedJsonNodeType, Instant expectedDate) {

        assertThat(accountHistory.getValue().getNodeType(), is(expectedJsonNodeType));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, Object expectedValue, Instant expectedDate) {

        assertThat(accountHistory.getValue().asText(), is(expectedValue.toString()));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory) {

        assertThat(accountHistory.getApplication(), is("test"));
        assertThat(accountHistory.getVersion(), is("v1"));
        assertThat(accountHistory.getUser(), is("user"));

    }
}
