package com.exemple.service.resource.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

}
