package com.exemple.service.resource.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.event.AccountEventResource;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.account.model.Account;
import com.exemple.service.resource.account.model.AccountEvent;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.account.model.Address;
import com.exemple.service.resource.account.model.Cgu;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Iterators;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class AccountResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountResource resource;

    private UUID id;

    private JsonNode account;

    @Autowired
    private AccountHistoryResource accountHistoryResource;

    @Autowired
    private AccountEventResource accountEventResource;

    @Autowired
    private CqlSession session;

    @BeforeMethod
    public void initExecutionContextDate() {

        OffsetDateTime now = OffsetDateTime.now();
        ServiceContextExecution.context().setDate(now);
        ServiceContextExecution.context().setPrincipal(() -> "user");
        ServiceContextExecution.context().setApp("test");
        ServiceContextExecution.context().setVersion("v1");

    }

    @Test
    public void save() {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");

        model.setAddresses(new HashMap<>());
        model.getAddresses().put("home", new Address("1 rue de la poste", null, null, null));
        model.getAddresses().put("job", new Address("1 rue de paris", null, null, 5));

        model.setCgus(new HashSet<>());
        model.getCgus().add(new Cgu("code_1", "v1"));
        model.getCgus().add(null);

        this.id = resource.save(JsonNodeUtils.create(model));

        this.account = JsonNodeUtils.create(model);
        this.account = JsonNodeFilterUtils.clean(this.account);
        JsonNodeUtils.set(this.account, id, AccountField.ID.field);

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(7)));

        assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), this.account.get("email"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), "1 rue de la poste",
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/street"), "1 rue de paris",
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job/floor"), 5,
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/code"), "code_1", ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/version"), "v1", ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), id.toString(), ServiceContextExecution.context().getDate().toInstant());

        AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
        assertThat(event.getEventType(), is(EventType.CREATE));
        assertThat(event.getLocalDate(), is(ServiceContextExecution.context().getDate().toLocalDate()));
        assertThat(event.getData(), is(notNullValue()));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all().size(), greaterThanOrEqualTo(1));

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode result = resource.get(id).get();

        assertThat(result.get("email"), is(this.account.get("email")));
        assertThat(result.get("addresses"), is(this.account.get("addresses")));
        assertThat(result.get("cgus"), is(this.account.get("cgus")));
        assertThat(result.get("id").asText(), is(id.toString()));

    }

    @Test
    public void getNotExist() {

        assertThat(resource.get(UUID.randomUUID()).isPresent(), is(false));

    }

    @Test(dependsOnMethods = "get")
    public void update() {

        Map<String, Object> model = new HashMap<>();
        model.put(AccountField.ID.field, id);

        Map<String, Address> addresses = new HashMap<>();
        addresses.put("home", new Address("10 rue de de la poste", null, null, 2));
        addresses.put("job", null);
        addresses.put("holidays", new Address("10 rue de paris", null, null, 5));
        model.put("addresses", addresses);

        Set<Cgu> cgus = new HashSet<>();
        Cgu cgu1 = new Cgu("code_1", "v2");
        cgus.add(cgu1);
        model.put("cgus", cgus);

        model.put("email", "jean.dupont@gmail.com");
        model.put("age", 19);

        AccountHistory previousHistoryEmail = accountHistoryResource.findByIdAndField(id, "/email");
        AccountHistory previousHistoryId = accountHistoryResource.findByIdAndField(id, "/id");
        AccountHistory previousHistoryCgus = accountHistoryResource.findByIdAndField(id, "/cgus/0/code");

        resource.save(JsonNodeUtils.create(model), this.account);

        this.account = resource.get(id).get();

        assertThat(this.account.get("addresses").get("home").get("street").asText(), is(addresses.get("home").getStreet()));
        assertThat(this.account.get("addresses").get("home").get("floor").asInt(), is(addresses.get("home").getFloor()));
        assertThat(this.account.get("addresses").get("home").get("city"), is(nullValue()));
        assertThat(this.account.get("addresses").get("home").get("zip"), is(nullValue()));

        assertThat(this.account.get("addresses").get("job"), is(nullValue()));

        assertThat(this.account.get("addresses").get("holidays").get("street").asText(), is(addresses.get("holidays").getStreet()));
        assertThat(this.account.get("addresses").get("holidays").get("floor").asInt(), is(addresses.get("holidays").getFloor()));
        assertThat(this.account.get("addresses").get("holidays").get("city"), is(nullValue()));
        assertThat(this.account.get("addresses").get("holidays").get("zip"), is(nullValue()));

        assertThat(this.account.get("age").asInt(), is(model.get("age")));
        assertThat(this.account.get("cgus"), hasItems(Iterators.toArray(JsonNodeUtils.create(cgus).elements(), JsonNode.class)));

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(10)));

        assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), previousHistoryEmail.getValue(), previousHistoryEmail.getDate());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/street"), addresses.get("home").getStreet(),
                ServiceContextExecution.context().getDate().toInstant(), JsonNodeType.STRING);
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/home/floor"), addresses.get("home").getFloor(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/job"), ServiceContextExecution.context().getDate().toInstant(),
                JsonNodeType.OBJECT);
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/holidays/street"), addresses.get("holidays").getStreet(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses/holidays/floor"), addresses.get("holidays").getFloor(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/code"), previousHistoryCgus.getValue(), previousHistoryCgus.getDate());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus/0/version"), cgu1.getVersion(),
                ServiceContextExecution.context().getDate().toInstant(), JsonNodeType.STRING);
        assertHistory(accountHistoryResource.findByIdAndField(id, "/age"), model.get("age"), ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), previousHistoryId.getValue(), previousHistoryId.getDate());

        AccountEvent event = accountEventResource.getByIdAndDate(id, ServiceContextExecution.context().getDate().toInstant());
        assertThat(event.getLocalDate(), is(ServiceContextExecution.context().getDate().toLocalDate()));
        assertThat(event.getEventType(), is(EventType.UPDATE));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "account_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all().size(), greaterThanOrEqualTo(2));
    }

    @Test(dependsOnMethods = "update")
    public void updateNull() {

        Map<String, Object> model = new HashMap<>();
        model.put(AccountField.ID.field, id);

        model.put("addresses", null);
        model.put("cgus", null);
        model.put("email", null);

        AccountHistory previousHistoryEmail = accountHistoryResource.findByIdAndField(id, "/email");
        AccountHistory previousHistoryAge = accountHistoryResource.findByIdAndField(id, "/age");
        AccountHistory previousHistoryId = accountHistoryResource.findByIdAndField(id, "/id");

        resource.save(JsonNodeUtils.create(model), this.account);

        this.account = resource.get(id).get();

        assertThat(this.account.get("addresses"), is(nullValue()));
        assertThat(this.account.get("email"), is(nullValue()));
        assertThat(this.account.get("age"), is(nullValue()));
        assertThat(this.account.get("cgus"), is(nullValue()));

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(5)));

        assertHistory(accountHistoryResource.findByIdAndField(id, "/email"), JsonNodeType.NULL, previousHistoryEmail.getValue(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/age"), JsonNodeType.NULL, previousHistoryAge.getValue(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "/cgus"), ServiceContextExecution.context().getDate().toInstant(),
                JsonNodeType.ARRAY);
        assertHistory(accountHistoryResource.findByIdAndField(id, "/addresses"), ServiceContextExecution.context().getDate().toInstant(),
                JsonNodeType.OBJECT);
        assertHistory(accountHistoryResource.findByIdAndField(id, "/id"), previousHistoryId.getValue(), previousHistoryId.getDate());

    }

    @Test
    public void findByIndex() {

        UUID id1 = resource.save(JsonNodeUtils.create(Collections.singletonMap("status", "NEW")));
        UUID id2 = resource.save(JsonNodeUtils.create(Collections.singletonMap("status", "NEW")));
        UUID id3 = resource.save(JsonNodeUtils.create(Collections.singletonMap("status", "OLD")));

        Set<JsonNode> accounts = resource.findByIndex("status", "NEW");

        List<String> ids = accounts.stream().map(account -> account.get(AccountField.ID.field).asText()).collect(Collectors.toList());

        assertThat(ids, hasSize(2));
        assertThat(ids, containsInAnyOrder(id1.toString(), id2.toString()));
        assertThat(ids, not(containsInAnyOrder(id3.toString())));
    }

    private static void assertHistory(AccountHistory accountHistory, JsonNode expectedValue, Instant expectedDate) {

        assertThat(accountHistory.getValue(), is(expectedValue));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue().getNodeType(), is(JsonNodeType.NULL));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, Object expectedValue, Instant expectedDate) {

        assertHistory(accountHistory, JsonNodeFilterUtils.clean(JsonNodeUtils.create(expectedValue)), expectedDate);

    }

    private static void assertHistory(AccountHistory accountHistory, Instant expectedDate, JsonNodeType expectedPreviousJsonNodeType) {

        assertThat(accountHistory.getValue().getNodeType(), is(JsonNodeType.NULL));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue().getNodeType(), is(expectedPreviousJsonNodeType));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, JsonNode expectedValue, Instant expectedDate,
            JsonNodeType expectedPreviousJsonNodeType) {

        assertThat(accountHistory.getValue(), is(expectedValue));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue().getNodeType(), is(expectedPreviousJsonNodeType));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, Object expectedValue, Instant expectedDate,
            JsonNodeType expectedPreviousJsonNodeType) {

        assertHistory(accountHistory, JsonNodeFilterUtils.clean(JsonNodeUtils.create(expectedValue)), expectedDate, expectedPreviousJsonNodeType);

    }

    private static void assertHistory(AccountHistory accountHistory, JsonNodeType expectedJsonNodeType, JsonNode expectedPreviousValue,
            Instant expectedDate) {

        assertThat(accountHistory.getValue().getNodeType(), is(expectedJsonNodeType));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue(), is(expectedPreviousValue));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory) {

        assertThat(accountHistory.getApplication(), is("test"));
        assertThat(accountHistory.getVersion(), is("v1"));
        assertThat(accountHistory.getUser(), is("user"));

    }
}
