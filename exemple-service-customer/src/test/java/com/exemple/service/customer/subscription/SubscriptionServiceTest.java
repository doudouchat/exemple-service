package com.exemple.service.customer.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.JsonNodeUtils;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.subscription.exception.SubscriptionServiceNotFoundException;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

@RecordApplicationEvents
@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class SubscriptionServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Autowired
    private SubscriptionService service;

    @Autowired
    private LoginResource loginResource;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource, loginResource);

    }

    @BeforeClass
    private void initServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
    }

    @DataProvider(name = "save")
    private static Object[][] update() {

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            return model;

        });

        return new Object[][] {
                // created
                { Optional.empty(), true },
                // updated
                { Optional.of(JsonNodeUtils.create(() -> source)), false }
                //
        };
    }

    @Test(dataProvider = "save")
    public void save(Optional<JsonNode> subscription, boolean expectedCreated) {

        // Given email

        String email = "jean@gmail.com";

        // And mock resource

        Mockito.when(resource.get(email)).thenReturn(subscription);

        // When perform save

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", email);
            return model;

        });

        boolean created = service.save(source);

        // Then check subscription

        assertThat(created, is(expectedCreated));

        // And check save resource

        JsonNode expectedSubscription = JsonNodeUtils.set(source, "subscription_date",
                new TextNode(ServiceContextExecution.context().getDate().toString()));

        ArgumentCaptor<JsonNode> subscriptionCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue(), is(expectedSubscription));

        // And check publish resource

        Optional<EventData> event = applicationEvents.stream(EventData.class).findFirst();
        assertThat(event.isPresent(), is(true));
        assertThat(event.get().getSource(), is(expectedSubscription));
        assertThat(event.get().getEventType(), is(EventType.CREATE));
        assertThat(event.get().getResource(), is("subscription"));

    }

    @Test
    public void get() throws SubscriptionServiceNotFoundException {

        // Given email

        String email = "jean@gmail.com";

        // And mock resource

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            return model;

        });

        Mockito.when(resource.get(email)).thenReturn(Optional.of(source));

        // When perform get

        JsonNode subscription = service.get(email);

        // Then check subscription

        assertThat(subscription, is(source));

    }

    @Test(expectedExceptions = SubscriptionServiceNotFoundException.class)
    public void getNotFound() throws SubscriptionServiceNotFoundException {

        // Given mock resource

        Mockito.when(resource.get(Mockito.anyString())).thenReturn(Optional.empty());

        // When perform get

        service.get("jean@gmail.com");

    }

}
