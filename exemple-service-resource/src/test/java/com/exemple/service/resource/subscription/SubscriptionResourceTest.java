package com.exemple.service.resource.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class SubscriptionResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SubscriptionResource resource;

    private String email;

    @Test
    public void save() {

        email = UUID.randomUUID() + "@gmail.com";

        Map<String, Object> model = new HashMap<>();
        model.put(SubscriptionField.EMAIL.field, email);

        resource.save(JsonNodeUtils.create(model));

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode subscription = resource.get(email).get();
        assertThat(subscription.get(SubscriptionField.EMAIL.field).textValue(), is(email));

    }

    @Test
    public void getNotFound() {

        assertThat(resource.get(UUID.randomUUID() + "@gmail.com").isPresent(), is(false));

    }

}
