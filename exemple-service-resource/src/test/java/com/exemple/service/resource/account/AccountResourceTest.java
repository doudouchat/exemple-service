package com.exemple.service.resource.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.data.ByteUtils;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.account.model.Account;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.account.model.Address;
import com.exemple.service.resource.account.model.Cgu;
import com.exemple.service.resource.account.model.Child;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Streams;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class AccountResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountResource resource;

    private UUID id;

    private JsonNode account;

    private byte[] schemaResource;

    @Autowired
    private AccountHistoryResource accountHistoryResource;

    @AfterClass
    public void executionContextDestroy() {

        ResourceExecutionContext.destroy();

        ResourceExecutionContext.get().setKeyspace("test");
    }

    @Test
    public void save() throws IOException {

        schemaResource = IOUtils.toByteArray(new ClassPathResource("test.json").getInputStream());

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setLastname("Dupont");
        model.setBirthday("1976-01-01");
        model.setAge(18);
        model.setSubscription_1(true);
        model.setCreation_date("2010-06-30 01:20:30.002Z");

        model.setAddress(new Address("1 rue de la paix", "Paris", "75002", 5));

        model.setAddresses(new HashMap<>());
        model.getAddresses().put("home", new Address("1 rue de de la poste", null, null, null));
        model.getAddresses().put("job", new Address("1 rue de paris", null, null, 5));

        model.setChildren(new HashMap<>());
        model.getChildren().put("2", new Child("2001-01-01"));

        model.setCgus(new HashSet<>());
        model.getCgus().add(new Cgu("code_1", "v1"));
        model.getCgus().add(null);

        model.setProfils(new HashSet<>());
        model.getProfils().add("profil 1");
        model.getProfils().add("profil 2");

        model.setPhones(new HashMap<>());
        model.getPhones().put("mobile", "0610203040");
        model.getPhones().put("fix", "0410203040");

        model.setNotes(new HashMap<>());
        model.getNotes().put("2001-01-01 00:00:00.000Z", "note 1");
        model.getNotes().put("2002-01-01 00:00:00.000Z", "note 2");

        model.setPreferences(new ArrayList<>());
        model.getPreferences().add(Arrays.asList("pref1", "value1", 10, "2001-01-01 00:00:00.000Z"));
        model.getPreferences().add(Arrays.asList("pref2", "value2", 500, "2002-01-01 00:00:00.000Z"));
        model.getPreferences().add(null);

        model.setContent(ByteUtils.toHexString(schemaResource));

        JsonNode account = JsonNodeUtils.create(model);

        this.id = UUID.randomUUID();

        this.account = resource.save(id, account);
        assertThat(this.account.get("email"), is(account.get("email")));
        assertThat(this.account.get("lastname"), is(account.get("lastname")));
        assertThat(this.account.get("birthday"), is(account.get("birthday")));
        assertThat(this.account.get("address"), is(account.get("address")));
        assertThat(this.account.get("addresses"), is(account.get("addresses")));
        assertThat(this.account.get("children"), is(account.get("children")));
        assertThat(this.account.get("cgus"), is(account.get("cgus")));
        assertThat(this.account.get("profiles"), is(account.get("profiles")));
        assertThat(this.account.get("phones"), is(account.get("phones")));
        assertThat(this.account.get("notes"), is(account.get("notes")));
        assertThat(this.account.findValue("id"), is(notNullValue()));
        assertThat(this.account.get("age"), is(account.get("age")));
        assertThat(this.account.get("subscription_1"), is(account.get("subscription_1")));
        assertThat(this.account.get("creation_date"), is(account.get("creation_date")));
        assertThat(this.account.get("preferences"), is(account.get("preferences")));
        assertThat(account.get("content"), is(this.account.get("content")));

        List<AccountHistory> histories = accountHistoryResource.findById(id);

        assertThat(histories, is(hasSize(9)));

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode account = resource.get(id).get();

        assertThat(account.get("email"), is(this.account.get("email")));
        assertThat(account.get("lastname"), is(this.account.get("lastname")));
        assertThat(account.get("birthday"), is(this.account.get("birthday")));
        assertThat(account.get("address"), is(this.account.get("address")));
        assertThat(account.get("addresses"), is(this.account.get("addresses")));
        assertThat(account.get("children"), is(this.account.get("children")));
        assertThat(account.get("cgus"), is(this.account.get("cgus")));
        assertThat(account.get("profiles"), is(this.account.get("profiles")));
        assertThat(account.get("phones"), is(this.account.get("phones")));
        assertThat(account.get("notes"), is(this.account.get("notes")));
        assertThat(account.findValue("id"), is(notNullValue()));
        assertThat(account.get("age"), is(this.account.get("age")));
        assertThat(account.get("subscription_1"), is(this.account.get("subscription_1")));
        assertThat(account.get("creation_date"), is(this.account.get("creation_date")));
        assertThat(account.get("preferences"), is(this.account.get("preferences")));
        assertThat(account.get("content"), is(this.account.get("content")));
    }

    @Test
    public void getNotExist() {

        assertThat(resource.get(UUID.randomUUID()).isPresent(), is(false));

    }

    private static Account account1 = new Account();

    static {

        account1.setLastname("Durand");
        account1.setBirthday("1976-02-01");
        account1.setAge(19);
        account1.setSubscription_1(false);

        account1.setAddresses(new HashMap<>());
        account1.getAddresses().put("home", new Address("10 rue de de la poste", null, null, 2));
        account1.getAddresses().put("job", null);
        account1.getAddresses().put("holidays", new Address("10 rue de paris", null, null, 5));
        account1.setAddress(new Address("20 rue de paris", "paris", null, 6));

        account1.setChildren(new HashMap<>());
        account1.getChildren().put("1", new Child("2002-01-01"));
        account1.getChildren().put("2", null);

        account1.setCgus(new HashSet<>());
        account1.getCgus().add(new Cgu("code_1", "v2"));
        account1.getCgus().add(null);

        account1.setProfils(new HashSet<>());
        account1.getProfils().add("profil 1");
        account1.getProfils().add("profil 3");
        account1.getProfils().add(null);

        account1.setPhones(new HashMap<>());
        account1.getPhones().put("mobile", "0699999999");
        account1.getPhones().put("office", "0199999999");
        account1.getPhones().put("fix", null);

        account1.setNotes(new HashMap<>());
        account1.getNotes().put("2001-01-01 00:00:00.000Z", "note 1a");
        account1.getNotes().put("2002-01-01 00:00:00.000Z", null);
        account1.getNotes().put("2003-01-01 00:00:00.506Z", "note 3");

        account1.setPreferences(new ArrayList<>());
        account1.getPreferences().add(Arrays.asList("pref1", "value2", 100, "2003-01-01 00:00:00.000Z"));
        account1.getPreferences().add(Arrays.asList("pref3", "value2", 500, "2002-01-01 00:00:00.000Z"));

    }

    @Test(dependsOnMethods = "get")
    public void update() {

        JsonNode model = JsonNodeUtils.create(account1);

        OffsetDateTime now = OffsetDateTime.now();
        ResourceExecutionContext.get().setDate(now);

        JsonNode account = resource.update(id, JsonNodeUtils.clone(model));

        JsonNodeFilterUtils.clean(model);

        assertThat(account.get("email"), is(model.get("email")));
        assertThat(account.get("lastname"), is(model.get("lastname")));
        assertThat(account.get("birthday"), is(model.get("birthday")));
        assertThat(account.get("addresses"), is(model.get("addresses")));
        assertThat(account.get("children"), is(model.get("children")));
        assertThat(account.get("cgus"), is(((ArrayNode) JsonNodeUtils.create(this.account.get("cgus"))).addAll((ArrayNode) model.get("cgus"))));
        assertThat(account.get("profiles"), is(model.get("profiles")));
        assertThat(account.get("phones"), is(model.get("phones")));
        assertThat(account.get("notes"), is(model.get("notes")));
        assertThat(account.findPath("password").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(account.get("age"), is(model.get("age")));
        assertThat(account.get("subscription_1"), is(model.get("subscription_1")));
        assertThat(account.get("creation_date"), is(model.get("creation_date")));
        assertThat(account.get("preferences"), is(model.get("preferences")));

        account = resource.get(id).get();

        JsonNodeFilterUtils.clean(account);

        assertThat(account.get("email"), is(model.get("email")));
        assertThat(account.get("lastname"), is(model.get("lastname")));
        assertThat(account.get("birthday"), is(model.get("birthday")));
        assertThat(account.get("addresses").get("home"), is(model.get("addresses").get("home")));
        assertThat(account.get("addresses").get("holidays"), is(model.get("addresses").get("holidays")));
        assertThat(account.get("addresses").get("job"), is(nullValue()));
        assertThat(account.get("children").get("1"), is(model.get("children").get("1")));
        assertThat(account.get("children").get("2"), is(nullValue()));

        Set<JsonNode> cgus = Streams.stream(account.get("cgus").elements()).collect(Collectors.toSet());

        assertThat(cgus, hasSize(2));
        assertThat(cgus, Matchers.hasItems(Streams.stream(model.get("cgus").elements()).collect(Collectors.toList()).toArray(new JsonNode[0])));
        assertThat(cgus,
                Matchers.hasItems(Streams.stream(this.account.get("cgus").elements()).collect(Collectors.toList()).toArray(new JsonNode[0])));
        assertThat(account.get("profiles"), is(model.get("profiles")));
        assertThat(account.get("phones").get("mobile"), is(model.get("phones").get("mobile")));
        assertThat(account.get("phones").get("job"), is(nullValue()));
        assertThat(account.get("phones").get("office"), is(model.get("phones").get("office")));
        assertThat(account.get("notes").get("2001-01-01 00:00:00Z"), is(model.get("notes").get("2001-01-01 00:00:00Z")));
        assertThat(account.get("notes").get("2002-01-01 00:00:00Z"), is((nullValue())));
        assertThat(account.get("notes").get("2003-01-01 00:00:00.506Z"), is(model.get("notes").get("2003-01-01 00:00:00.506Z")));
        assertThat(account.findValue("id").asText(), is(id.toString()));
        assertThat(account.findPath("password").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(account.get("age"), is(model.get("age")));
        assertThat(account.get("subscription_1"), is(model.get("subscription_1")));
        assertThat(account.get("creation_date"), is(model.get("creation_date")));
        assertThat(account.get("preferences"), is(model.get("preferences")));

        List<AccountHistory> histories = accountHistoryResource.findById(id).stream().filter(h -> h.getDate().equals(now.toInstant()))
                .collect(Collectors.toList());

        assertThat(histories, is(hasSize(10)));
    }

    @DataProvider(name = "accounts")
    public static Object[][] accounts() {

        Map<String, Object> account2 = new HashMap<>();
        account2.put("phones2", Collections.singletonMap("home", "061213"));

        Map<String, Object> account3 = new HashMap<>();
        account3.put("phones2", Collections.singletonMap("home", null));

        Map<String, Object> account4 = new HashMap<>();
        account4.put("cgus2", Collections.singleton(UUID.randomUUID()));

        return new Object[][] {

                { JsonNodeUtils.create(account1), 0 },

                { JsonNodeUtils.create(account2), 1 },

                { JsonNodeUtils.create(account3), 1 },

                { JsonNodeUtils.create(account4), 1 }

        };

    }

    @Test(dependsOnMethods = "update", dataProvider = "accounts")
    public void patch(JsonNode model, int expectedHistories) {

        OffsetDateTime now = OffsetDateTime.now();
        ResourceExecutionContext.get().setDate(now);

        JsonNode account = resource.update(id, model);
        JsonNode origin = resource.get(id).get();

        account.fields().forEachRemaining(
                (Map.Entry<String, JsonNode> node) -> assertThat(node.getKey() + " no match", node.getValue(), is(origin.get(node.getKey()))));

        List<AccountHistory> histories = accountHistoryResource.findById(id).stream().filter(h -> h.getDate().equals(now.toInstant()))
                .collect(Collectors.toList());

        assertThat(histories, hasSize(expectedHistories));
    }

    @Test(dependsOnMethods = "patch")
    public void updateOther() {

        OffsetDateTime now = OffsetDateTime.now();
        ResourceExecutionContext.get().setDate(now);

        resource.update(id, JsonNodeUtils.init("addresses"));

        account = resource.get(id).get();
        JsonNodeFilterUtils.clean(account);

        assertThat(account.path("addresses").getNodeType(), is(JsonNodeType.MISSING));

        List<AccountHistory> histories = accountHistoryResource.findById(id).stream().filter(h -> h.getDate().equals(now.toInstant()))
                .collect(Collectors.toList());

        assertThat(histories, is(hasSize(1)));
    }

    @Test
    public void findByIndex() {

        final String status = "NEW";

        Account model1 = new Account();
        model1.setStatus(status);
        String id1 = resource.save(UUID.randomUUID(), JsonNodeUtils.create(model1)).get(AccountField.ID.field).asText();

        Account model2 = new Account();
        model2.setStatus(status);
        String id2 = resource.save(UUID.randomUUID(), JsonNodeUtils.create(model2)).get(AccountField.ID.field).asText();

        Account model3 = new Account();
        model3.setStatus("OLD");
        String id3 = resource.save(UUID.randomUUID(), JsonNodeUtils.create(model3)).get(AccountField.ID.field).asText();

        Set<JsonNode> accounts = resource.findByIndex("status", status);

        List<String> ids = accounts.stream().map(account -> account.get(AccountField.ID.field).asText()).collect(Collectors.toList());

        assertThat(ids, hasSize(2));
        assertThat(ids, containsInAnyOrder(id1, id2));
        assertThat(ids, not(containsInAnyOrder(id3)));
    }
}
