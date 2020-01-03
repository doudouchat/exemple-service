package com.exemple.service.api.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;

public class PatchUtilsTest {

    @DataProvider(name = "patchs")
    private static Object[][] patch() {

        // add field

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "add");
        patch0.put("path", "/lastname");
        patch0.put("value", "Dupond");

        Map<String, Object> source0 = new HashMap<>();
        source0.put("lastname", null);

        Map<String, Object> expected0 = new HashMap<>();
        expected0.put("lastname", "Dupond");

        // replace field

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "replace");
        patch1.put("path", "/lastname");
        patch1.put("value", "Dupond");

        Map<String, Object> source1 = new HashMap<>();
        source1.put("lastname", "Dupont");

        Map<String, Object> expected1 = new HashMap<>();
        expected1.put("lastname", "Dupond");

        // replace object

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "replace");
        patch2.put("path", "/addresses/job");
        patch2.put("value", Collections.singletonMap("city", "Paris"));

        Map<String, Object> addresses2 = new HashMap<>();
        addresses2.put("job", Collections.singletonMap("city", "Lyon"));
        addresses2.put("home", Collections.singletonMap("city", "New York"));

        Map<String, Object> source2 = new HashMap<>();
        source2.put("addresses", addresses2);

        Map<String, Object> expected2 = new HashMap<>();
        expected2.put("addresses", Collections.singletonMap("job", Collections.singletonMap("city", "Paris")));

        // replace object field

        Map<String, Object> patch3 = new HashMap<>();
        patch3.put("op", "replace");
        patch3.put("path", "/addresses/job/city");
        patch3.put("value", "Paris");

        // replace list

        Map<String, Object> patch4 = new HashMap<>();
        patch4.put("op", "replace");
        patch4.put("path", "/cgus/0/city");
        patch4.put("value", "Paris");

        Map<String, Object> source4 = new HashMap<>();
        source4.put("cgus", Collections.singletonList(Collections.singletonMap("city", "Lyon")));

        Map<String, Object> expected4 = new HashMap<>();
        expected4.put("cgus", Collections.singletonList(Collections.singletonMap("city", "Paris")));

        // remove field

        Map<String, Object> patch5 = new HashMap<>();
        patch5.put("op", "remove");
        patch5.put("path", "/lastname");

        Map<String, Object> source5 = new HashMap<>();
        source5.put("lastname", "Dupond");

        Map<String, Object> expected5 = new HashMap<>();
        expected5.put("lastname", null);

        // remove object

        Map<String, Object> patch6 = new HashMap<>();
        patch6.put("op", "remove");
        patch6.put("path", "/addresses/job");

        Map<String, Object> expected6 = new HashMap<>();
        expected6.put("addresses", Collections.singletonMap("job", null));

        // copy object

        Map<String, Object> patch7 = new HashMap<>();
        patch7.put("op", "copy");
        patch7.put("path", "/addresses/job");
        patch7.put("from", "/addresses/home");

        Map<String, Object> expected7 = new HashMap<>();
        expected7.put("addresses", Collections.singletonMap("job", Collections.singletonMap("city", "New York")));

        // move object

        Map<String, Object> patch8 = new HashMap<>();
        patch8.put("op", "move");
        patch8.put("path", "/addresses/job");
        patch8.put("from", "/addresses/home");

        Map<String, Object> expectedAddresses8 = new HashMap<>();
        expectedAddresses8.put("job", Collections.singletonMap("city", "New York"));
        expectedAddresses8.put("home", null);

        Map<String, Object> expected8 = new HashMap<>();
        expected8.put("addresses", expectedAddresses8);

        // add list

        Map<String, Object> patch9 = new HashMap<>();
        patch9.put("op", "add");
        patch9.put("path", "/cgus/0");
        patch9.put("value", Collections.singletonMap("city", "Paris"));

        Map<String, Object> source9 = new HashMap<>();
        source9.put("cgus", Collections.singletonList(Collections.singletonMap("city", "Lyon")));

        List<Map<String, Object>> cgus9 = new ArrayList<>();
        cgus9.add(Collections.singletonMap("city", "Paris"));
        cgus9.add(Collections.singletonMap("city", "Lyon"));

        Map<String, Object> expected9 = new HashMap<>();
        expected9.put("cgus", cgus9);

        // add object

        Map<String, Object> patch10 = new HashMap<>();
        patch10.put("op", "add");
        patch10.put("path", "/addresses");
        patch10.put("value", Collections.singletonMap("job", Collections.singletonMap("city", "Paris")));

        Map<String, Object> source10 = new HashMap<>();

        Map<String, Object> expected10 = new HashMap<>();
        expected10.put("addresses", Collections.singletonMap("job", Collections.singletonMap("city", "Paris")));

        // remove object

        Map<String, Object> patch11 = new HashMap<>();
        patch11.put("op", "remove");
        patch11.put("path", "/addresses");

        Map<String, Object> source11 = new HashMap<>();
        source11.put("addresses", Collections.singletonMap("job", Collections.singletonMap("city", "Paris")));

        Map<String, Object> expected11 = new HashMap<>();
        expected11.put("addresses", null);

        return new Object[][] {
                // add field
                { patch0, source0, expected0 },
                // replace field
                { patch1, source1, expected1 },
                // replace object
                { patch2, source2, expected2 },
                // replace object field
                { patch3, source2, expected2 },
                // replace list
                { patch4, source4, expected4 },
                // remove field
                { patch5, source5, expected5 },
                // remove object
                { patch6, source2, expected6 },
                // copy object
                { patch7, source2, expected7 },
                // move object
                { patch8, source2, expected8 },
                // add list
                { patch9, source9, expected9 },
                // add object
                { patch10, source10, expected10 },
                // remove object
                { patch11, source11, expected11 }
                //
        };
    }

    @Test(dataProvider = "patchs")
    public void diff(Map<String, Object> patch, Map<String, Object> source, Map<String, Object> expected) {

        JsonNode diff = PatchUtils.diff(JsonNodeUtils.create(Collections.singletonList(patch)), JsonNodeUtils.create(source));

        assertThat(diff, is(notNullValue()));
        assertThat(diff, is(JsonNodeUtils.create(expected)));

    }

}
