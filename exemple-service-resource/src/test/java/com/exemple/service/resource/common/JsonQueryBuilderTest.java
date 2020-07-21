package com.exemple.service.resource.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.data.ByteUtils;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.resource.account.model.Address;
import com.exemple.service.resource.account.model.Cgu;
import com.exemple.service.resource.account.model.Child;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterators;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class JsonQueryBuilderTest extends AbstractTestNGSpringContextTests {

    private JsonQueryBuilder resource;

    @Autowired
    private CqlSession session;

    private UUID id;

    private byte[] schemaResource;

    private JsonNode exemple;

    @BeforeClass
    public void buildJsonQueryBuilder() {

        resource = new JsonQueryBuilder(session, "exemple");
    }

    @AfterClass
    public void executionContextDestroy() {

        ResourceExecutionContext.destroy();
    }

    @Test
    public void save() throws IOException {

        this.id = UUID.randomUUID();
        schemaResource = IOUtils.toByteArray(new ClassPathResource("test.json").getInputStream());

        Map<String, Object> model = new HashMap<>();
        model.put("id", id);
        model.put("email", "jean.dupont@gmail.com");
        model.put("birthday", "1976-01-01");
        model.put("age", 18);
        model.put("enabled", true);
        model.put("creation_date", "2010-06-30 01:20:30.002Z");
        model.put("address", new Address("1 rue de la paix", "Paris", "75002", 5));

        Map<String, Object> addresses = new HashMap<>();
        addresses.put("home", new Address("1 rue de de la poste", null, null, null));
        addresses.put("job", new Address("1 rue de paris", null, null, 5));
        model.put("addresses", addresses);

        Map<String, Object> children = new HashMap<>();
        children.put("2", new Child("2001-01-01"));
        model.put("children", children);

        Set<Object> cgus = new HashSet<>();
        cgus.add(new Cgu("code_1", "v1"));
        cgus.add(null);
        model.put("cgus", cgus);

        Set<Object> profils = new HashSet<>();
        profils.add("profil 1");
        profils.add("profil 2");
        model.put("profils", profils);

        Map<String, Object> phones = new HashMap<>();
        phones.put("mobile", "0610203040");
        phones.put("fix", "0410203040");
        model.put("phones", phones);

        Map<String, Object> notes = new HashMap<>();
        notes.put("2001-01-01 00:00:00.000Z", "note 1");
        notes.put("2002-01-01 00:00:00.000Z", "note 2");
        model.put("notes", notes);

        List<Object> preferences = new ArrayList<>();
        preferences.add(Arrays.asList("pref1", "value1", 10, "2001-01-01 00:00:00.000Z"));
        preferences.add(Arrays.asList("pref2", "value2", 500, "2002-01-01 00:00:00.000Z"));
        preferences.add(null);
        model.put("preferences", preferences);

        model.put("content", ByteUtils.toHexString(schemaResource));

        session.execute(resource.insert(JsonNodeUtils.create(model)).build());

        this.exemple = get(id);
        this.exemple = JsonNodeFilterUtils.clean(this.exemple);

        JsonNode expected = JsonNodeUtils.create(model);
        expected = JsonNodeFilterUtils.clean(expected);

        assertThat(this.exemple.get("email"), is(expected.get("email")));
        assertThat(this.exemple.get("birthday"), is(expected.get("birthday")));
        assertThat(this.exemple.get("address"), is(expected.get("address")));
        assertThat(this.exemple.get("addresses"), is(expected.get("addresses")));
        assertThat(this.exemple.get("children"), is(expected.get("children")));
        assertThat(this.exemple.get("cgus"), is(expected.get("cgus")));
        assertThat(this.exemple.get("profiles"), is(expected.get("profiles")));
        assertThat(this.exemple.get("phones"), is(expected.get("phones")));
        assertThat(this.exemple.get("notes"), is(expected.get("notes")));
        assertThat(this.exemple.get("age"), is(expected.get("age")));
        assertThat(this.exemple.get("enabled"), is(expected.get("enabled")));
        assertThat(this.exemple.get("creation_date"), is(expected.get("creation_date")));
        assertThat(this.exemple.get("preferences"), is(expected.get("preferences")));
        assertThat(this.exemple.get("content"), is(expected.get("content")));

    }

    private JsonNode get(UUID id) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), "exemple").json().all().whereColumn("id")
                .isEqualTo(QueryBuilder.literal(id));

        return session.execute(select.build()).one().get(0, JsonNode.class);
    }

    @DataProvider(name = "patchs")
    public static Object[][] patchs() {

        Map<String, Object> addresses = new HashMap<>();
        addresses.put("home", new Address("10 rue de de la poste", null, null, 2));
        addresses.put("job", null);
        addresses.put("holidays", new Address("10 rue de paris", null, null, 5));

        Map<String, Object> children = new HashMap<>();
        children.put("1", new Child("2002-01-01"));
        children.put("2", null);

        Map<String, Object> phones = new HashMap<>();
        phones.put("mobile", "0699999999");
        phones.put("office", "0199999999");
        phones.put("fix", null);

        Map<String, Object> notes = new HashMap<>();
        notes.put("2001-01-01 00:00:00.000Z", "note 1a");
        notes.put("2002-01-01 00:00:00.000Z", null);
        notes.put("2003-01-01 00:00:00.506Z", "note 3");

        List<Object> preferences = new ArrayList<>();
        preferences.add(Arrays.asList("pref1", "value2", 100, "2003-01-01 00:00:00.000Z"));
        preferences.add(Arrays.asList("pref3", "value2", 500, "2002-01-01 00:00:00.000Z"));

        return new Object[][] {

                // delete email
                { Collections.singletonMap("email", null) },

                // update birthday
                { Collections.singletonMap("birthday", "1976-02-01") },

                // update age
                { Collections.singletonMap("age", 19) },

                // update enabled
                { Collections.singletonMap("enabled", false) },

                // update address
                { Collections.singletonMap("address", new Address("20 rue de paris", "paris", null, 6)) },

                // update addresses
                { Collections.singletonMap("addresses", addresses) },

                // delete addresses
                { Collections.singletonMap("addresses", null) },

                // update children
                { Collections.singletonMap("children", children) },

                // update phones
                { Collections.singletonMap("phones", phones) },

                // update notes
                { Collections.singletonMap("notes", notes) },

                // update preferences
                { Collections.singletonMap("preferences", preferences) }

        };

    }

    @Test(dependsOnMethods = "save", dataProvider = "patchs")
    public void update(Map<String, Object> patch) {

        session.execute(resource.update(JsonNodeUtils.create(patch)).whereColumn("id").isEqualTo(QueryBuilder.literal(id)).build());

        this.exemple = JsonNodeFilterUtils.clean(get(id));

        JsonNode expected = JsonNodeUtils.create(patch);
        expected = JsonNodeFilterUtils.clean(expected);

        String property = patch.keySet().iterator().next();
        assertThat(this.exemple.get(property), is(expected.get(property)));
    }

    @DataProvider(name = "sets")
    public static Object[][] sets() {

        Set<Object> cgus = new HashSet<>();
        cgus.add(new Cgu("code_1", "v2"));
        cgus.add(null);

        Set<Object> profils = new HashSet<>();
        profils.add("profil 1");
        profils.add("profil 3");
        profils.add(null);

        return new Object[][] {

                // update cgus
                { cgus, "cgus" },

                // update profils
                { profils, "profils" }

        };

    }

    @Test(dependsOnMethods = "save", dataProvider = "sets")
    public void updateSet(Set<Object> patch, String property) {

        ObjectMapper mapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Set<Object> previousValues = mapper.convertValue(this.exemple.get(property), Set.class);

        session.execute(resource.update(JsonNodeUtils.create(Collections.singletonMap(property, patch))).whereColumn("id")
                .isEqualTo(QueryBuilder.literal(id)).build());

        this.exemple = JsonNodeFilterUtils.clean(get(id));

        previousValues.addAll(patch);

        JsonNode expected = JsonNodeUtils.create(Collections.singletonMap(property, previousValues));
        expected = JsonNodeFilterUtils.clean(expected);

        assertThat(this.exemple.get(property).size(), is(expected.get(property).size()));
        assertThat(this.exemple.get(property), Matchers.hasItems(Iterators.toArray(((ArrayNode) expected.get(property)).elements(), JsonNode.class)));

    }

    @Test(dependsOnMethods = "updateSet")
    public void copy() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("birthday", "1977-01-01");

        Set<Object> profils = new HashSet<>();
        profils.add("profil 4");
        model.put("profils", profils);

        session.execute(resource.copy(this.exemple, JsonNodeUtils.create(model)).build());

        JsonNode expected = get(id);
        expected = JsonNodeFilterUtils.clean(expected);

        JsonNode actual = JsonNodeUtils.create(model);
        actual = JsonNodeFilterUtils.clean(actual);

        assertThat(actual.get("email"), is(expected.get("email")));
        assertThat(actual.get("birthday"), is(expected.get("birthday")));
        assertThat(this.exemple.get("address"), is(expected.get("address")));
        assertThat(this.exemple.get("addresses"), is(expected.get("addresses")));
        assertThat(this.exemple.get("children"), is(expected.get("children")));
        assertThat(this.exemple.get("cgus"), is(expected.get("cgus")));
        assertThat(this.exemple.get("profiles"), is(expected.get("profiles")));
        assertThat(this.exemple.get("phones"), is(expected.get("phones")));
        assertThat(this.exemple.get("notes"), is(expected.get("notes")));
        assertThat(this.exemple.get("age"), is(expected.get("age")));
        assertThat(this.exemple.get("enabled"), is(expected.get("enabled")));
        assertThat(this.exemple.get("creation_date"), is(expected.get("creation_date")));
        assertThat(this.exemple.get("preferences"), is(expected.get("preferences")));
        assertThat(this.exemple.get("content"), is(expected.get("content")));

        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Set<Object> actualProfils = mapper.convertValue(this.exemple.get("profils"), Set.class);

        assertThat(actualProfils.size(), is(4));
        assertThat(actualProfils, Matchers.containsInAnyOrder("profil 1", "profil 2", "profil 3", "profil 4"));

    }
}
