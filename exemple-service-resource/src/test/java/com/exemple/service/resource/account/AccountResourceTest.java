package com.exemple.service.resource.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
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

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.account.model.Account;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.account.model.Address;
import com.exemple.service.resource.account.model.Cgu;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
        model.getAddresses().put("home", new Address("1 rue de de la poste", null, null, null));
        model.getAddresses().put("job", new Address("1 rue de paris", null, null, 5));

        model.setCgus(new HashSet<>());
        model.getCgus().add(new Cgu("code_1", "v1"));
        model.getCgus().add(null);

        this.id = UUID.randomUUID();

        this.account = resource.save(id, JsonNodeUtils.create(model));

        JsonNode expected = JsonNodeUtils.create(model);
        JsonNodeFilterUtils.clean(expected);

        assertThat(this.account.get("email"), is(expected.get("email")));
        assertThat(this.account.get("addresses"), is(expected.get("addresses")));
        assertThat(this.account.get("cgus"), is(expected.get("cgus")));
        assertThat(this.account.get("id").asText(), is(id.toString()));

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(5)));

        accountHistoryResource.findByIdAndField(id, "email");

        System.out.println(accountHistoryResource.findByIdAndField(id, "addresses/home").getValue());

        assertHistory(accountHistoryResource.findByIdAndField(id, "email"), expected.get("email"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "addresses/home"), expected.get("addresses").get("home"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "addresses/job"), expected.get("addresses").get("job"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "cgus"), expected.get("cgus"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "id"), id.toString(), ServiceContextExecution.context().getDate().toInstant());

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode result = resource.get(id).get();

        assertThat(result.get("email"), is(this.account.get("email")));
        assertThat(result.get("addresses"), is(this.account.get("addresses")));
        assertThat(result.get("cgus"), is(this.account.get("cgus")));
        assertThat(result.get("id"), is(this.account.get("id")));

    }

    @Test
    public void getNotExist() {

        assertThat(resource.get(UUID.randomUUID()).isPresent(), is(false));

    }

    @Test(dependsOnMethods = "get")
    public void update() {

        Map<String, Object> model = new HashMap<>();

        Map<String, Address> addresses = new HashMap<>();
        addresses.put("home", new Address("10 rue de de la poste", null, null, 2));
        addresses.put("job", null);
        addresses.put("holidays", new Address("10 rue de paris", null, null, 5));
        model.put("addresses", addresses);

        Set<Cgu> cgus = new HashSet<>();
        cgus.add(new Cgu("code_1", "v2"));
        model.put("cgus", cgus);

        model.put("email", "jean.dupont@gmail.com");
        model.put("age", 19);

        AccountHistory previousHistoryEmail = accountHistoryResource.findByIdAndField(id, "email");
        AccountHistory previousHistoryId = accountHistoryResource.findByIdAndField(id, "id");
        AccountHistory previousHistoryHome = accountHistoryResource.findByIdAndField(id, "addresses/home");
        AccountHistory previousHistoryJob = accountHistoryResource.findByIdAndField(id, "addresses/job");
        AccountHistory previousHistoryCgus = accountHistoryResource.findByIdAndField(id, "cgus");

        this.account = resource.update(id, JsonNodeUtils.create(model));

        JsonNode expected = JsonNodeUtils.create(model);
        JsonNodeFilterUtils.clean(expected);

        assertThat(this.account.get("addresses"), is(expected.get("addresses")));
        assertThat(this.account.get("age"), is(expected.get("age")));
        assertThat(this.account.get("cgus"), hasItems(Iterators.toArray(((ArrayNode) expected.get("cgus")).elements(), JsonNode.class)));

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(7)));

        assertHistory(accountHistoryResource.findByIdAndField(id, "email"), previousHistoryEmail.getValue(), previousHistoryEmail.getDate());
        assertHistory(accountHistoryResource.findByIdAndField(id, "addresses/home"), expected.get("addresses").get("home"),
                previousHistoryHome.getValue(), ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "addresses/job"), JsonNodeType.NULL, previousHistoryJob.getValue(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "addresses/holidays"), expected.get("addresses").get("holidays"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "cgus"), expected.get("cgus"), previousHistoryCgus.getValue(),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "age"), expected.get("age"),
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "id"), previousHistoryId.getValue(), previousHistoryId.getDate());
    }

    @Test(dependsOnMethods = "update")
    public void updateNull() {

        Map<String, Object> model = new HashMap<>();

        model.put("addresses", null);
        model.put("age", null);

        this.account = resource.update(id, JsonNodeUtils.create(model));

        JsonNode expected = JsonNodeUtils.create(model);
        JsonNodeFilterUtils.clean(expected);

        assertThat(this.account.get("addresses"), is(nullValue()));
        assertThat(this.account.get("age"), is(nullValue()));

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(8)));

        assertHistory(accountHistoryResource.findByIdAndField(id, "addresses"), JsonNodeType.NULL,
                ServiceContextExecution.context().getDate().toInstant());
        assertHistory(accountHistoryResource.findByIdAndField(id, "age"), JsonNodeType.NULL, ServiceContextExecution.context().getDate().toInstant());

    }

    @Test
    public void findByIndex() {

        String id1 = resource.save(UUID.randomUUID(), JsonNodeUtils.create(Collections.singletonMap("status", "NEW"))).get(AccountField.ID.field)
                .asText();
        String id2 = resource.save(UUID.randomUUID(), JsonNodeUtils.create(Collections.singletonMap("status", "NEW"))).get(AccountField.ID.field)
                .asText();
        String id3 = resource.save(UUID.randomUUID(), JsonNodeUtils.create(Collections.singletonMap("status", "OLD"))).get(AccountField.ID.field)
                .asText();

        Set<JsonNode> accounts = resource.findByIndex("status", "NEW");

        List<String> ids = accounts.stream().map(account -> account.get(AccountField.ID.field).asText()).collect(Collectors.toList());

        assertThat(ids, hasSize(2));
        assertThat(ids, containsInAnyOrder(id1, id2));
        assertThat(ids, not(containsInAnyOrder(id3)));
    }

    private static void assertHistory(AccountHistory accountHistory, JsonNode expectedValue, Instant expectedDate) {

        assertThat(accountHistory.getValue(), is(expectedValue));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue(), is(nullValue()));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, JsonNode expectedValue, JsonNode expectedPreviousValue, Instant expectedDate) {

        assertThat(accountHistory.getValue(), is(expectedValue));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue(), is(expectedPreviousValue));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, String expectedValue, Instant expectedDate) {

        assertThat(accountHistory.getValue().textValue(), is(expectedValue));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertThat(accountHistory.getPreviousValue(), is(nullValue()));
        assertHistory(accountHistory);

    }

    private static void assertHistory(AccountHistory accountHistory, JsonNodeType expectedJsonNodeType, Instant expectedDate) {

        assertThat(accountHistory.getValue().getNodeType(), is(expectedJsonNodeType));
        assertThat(accountHistory.getDate(), is(expectedDate));
        assertHistory(accountHistory);

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
