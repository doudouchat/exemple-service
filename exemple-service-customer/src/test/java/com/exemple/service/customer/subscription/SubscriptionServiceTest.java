package com.exemple.service.customer.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.subscription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class SubscriptionServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private SubscriptionService service;

    @Autowired
    private LoginResource loginResource;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource, loginResource);

    }

    @DataProvider(name = "save")
    private static Object[][] update() {

        return new Object[][] {
                // created
                { Optional.empty(), true },
                // updated
                { Optional.of(JsonNodeUtils.init()), false }
                //
        };
    }

    @Test(dataProvider = "save")
    public void save(Optional<JsonNode> subscription, boolean expectedCreated) {

        String email = "jean@gmail.com";

        Mockito.when(resource.get(email)).thenReturn(subscription);
        Mockito.doNothing().when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put(SubscriptionField.EMAIL.field, email);

        boolean created = service.save(JsonNodeUtils.create(model));

        assertThat(created, is(expectedCreated));

        Mockito.verify(resource).get(email);
        Mockito.verify(resource).save(Mockito.any(JsonNode.class));

    }

    @Test
    public void get() throws SubscriptionServiceNotFoundException {

        String email = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(email))).thenReturn(Optional.of(JsonNodeUtils.init()));

        JsonNode data = service.get(email);

        assertThat(data, is(notNullValue()));

        Mockito.verify(resource).get(Mockito.eq(email));

    }

    @Test(expectedExceptions = SubscriptionServiceNotFoundException.class)
    public void getNotFound() throws SubscriptionServiceNotFoundException {

        String email = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(email))).thenReturn(Optional.empty());

        service.get(email);

    }

}
