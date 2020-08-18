package com.exemple.service.resource.common.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.exemple.service.resource.account.model.Address;
import com.exemple.service.resource.account.model.Cgu;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

public class JsonNodeFilterUtilsTest {

    @Test
    public void clean() {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("birthday", null);
        Address address = new Address("1 rue de la paix", null);
        model.put("address", address);

        Map<String, Object> addresses = new HashMap<>();
        Address home = new Address("1 rue de de la poste", null);
        addresses.put("home", home);
        addresses.put("job", null);
        model.put("addresses", addresses);

        Cgu cgu = new Cgu("code_1", null);
        Set<Object> cgus = new HashSet<>();
        cgus.add(cgu);
        cgus.add(null);
        model.put("cgus", cgus);

        String profil = "profil 1";
        Set<Object> profils = new HashSet<>();
        profils.add(profil);
        profils.add(null);
        model.put("profils", profils);

        List<?> preference = Arrays.asList("pref1", 10);
        List<Object> preferences = new ArrayList<>();
        preferences.add(preference);
        preferences.add(null);
        model.put("preferences", preferences);

        JsonNode source = JsonNodeFilterUtils.clean(JsonNodeUtils.create(model));

        assertThat(source.path("email").textValue(), is(model.get("email")));
        assertThat(source.path("birthday").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(source.path("address").path("street").textValue(), is(address.getStreet()));
        assertThat(source.path("address").path("city").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(source.path("addresses").path("home").path("street").textValue(), is(home.getStreet()));
        assertThat(source.path("addresses").path("home").path("city").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(source.path("addresses").path("job").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(source.path("cgus").size(), is(1));
        assertThat(source.path("cgus").get(0).path("code").textValue(), is(cgu.getCode()));
        assertThat(source.path("cgus").get(0).path("version").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(source.path("profils").size(), is(1));
        assertThat(source.path("profils").get(0).textValue(), is(profil));
        assertThat(source.path("preferences").size(), is(1));
        assertThat(source.path("preferences").get(0).get(0).textValue(), is(preference.get(0)));
        assertThat(source.path("preferences").get(0).get(1).intValue(), is(preference.get(1)));

    }

}
