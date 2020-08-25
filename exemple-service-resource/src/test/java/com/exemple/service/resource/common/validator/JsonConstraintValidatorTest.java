package com.exemple.service.resource.common.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import javax.validation.ConstraintViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.account.model.Account;
import com.exemple.service.resource.account.model.Address;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class JsonConstraintValidatorTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountResource resource;

    @Test
    public void success() {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setBirthday(null);

        model.setAddress(new Address("1 rue de la paix", "Paris", "75002", 5));

        model.setAddresses(new HashMap<>());
        model.getAddresses().put("home", new Address("1 rue de de la poste", null, null, null));

        model.setProfils(new HashSet<>());
        model.getProfils().add("profil 1");

        model.setPreferences(new ArrayList<>());
        model.getPreferences().add(Arrays.asList("pref1", "value1", 10, "2001-01-01 00:00:00.000Z"));

        UUID id = UUID.randomUUID();

        JsonNode account = resource.save(id, JsonNodeUtils.create(model));
        assertThat(account.get("email"), is(notNullValue()));
        assertThat(account.get("address"), is(notNullValue()));
        assertThat(account.get("addresses"), is(notNullValue()));
        assertThat(account.get("profils"), is(notNullValue()));
        assertThat(account.get("preferences"), is(notNullValue()));

    }

    @DataProvider(name = "failures")
    public static Object[][] failures() {

        return new Object[][] {
                // text failure
                { "email", 10 },
                // int failure
                { "age", "age" },
                // field unknown
                { "nc", "nc" },
                // date failure
                { "birthday", "2019-02-30" }, { "birthday", "aaa" },
                // timestamp failure
                { "creation_date", "2019-02-30T10:00:00Z" }, { "creation_date", "aaa" },
                // boolean failure
                { "enabled", 10 },
                // map failure
                { "addresses", 10 },
                // map int failure
                { "addresses", Collections.singletonMap("home", Collections.singletonMap("floor", "toto")) },
                // map field unknown
                { "addresses", Collections.singletonMap("home", Collections.singletonMap("nc", "toto")) },
                // map boolean failure
                { "addresses", Collections.singletonMap("home", Collections.singletonMap("enable", "toto")) },
                // map date failure
                { "children", Collections.singletonMap("1", Collections.singletonMap("birthday", "2019-02-30")) },
                { "children", Collections.singletonMap("1", Collections.singletonMap("birthday", "aaa")) },
                // map index int failure
                { "children", Collections.singletonMap("aaa", Collections.singletonMap("birthday", "2001-01-01")) },
                // map index timestamp failure
                { "notes", Collections.singletonMap("2019-02-30T10:00:00Z", "note 1") }, { "notes", Collections.singletonMap("aaa", "note 1") },
                // set failure
                { "cgus", 10 },
                // list failure
                { "preferences", 10 },
                // tuple int failure
                { "preferences", Arrays.asList(Arrays.asList("pref2", "value2", "aaa", "2002-01-01 00:00:00.000Z")) },
                // tuple timestamp failure
                { "preferences", Arrays.asList(Arrays.asList("pref2", "value2", 100, "aaa")) },
                // tuple too fields
                { "preferences", Arrays.asList(Arrays.asList("pref2", "value2", 100, "2002-01-01 00:00:00.000Z", "new")) },
                // tuple fields missing
                { "preferences", Arrays.asList(Arrays.asList("pref2", "value2", 100)) } };

    }

    @Test(dataProvider = "failures", expectedExceptions = ConstraintViolationException.class)
    public void saveFailure(String property, Object value) {

        JsonNode node = JsonNodeUtils.clone(JsonNodeUtils.create(new Account()));
        JsonNodeUtils.set(node, value, property);

        resource.save(UUID.randomUUID(), node);
    }
}
