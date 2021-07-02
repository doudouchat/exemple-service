package com.exemple.service.resource.subscription;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.subscription.event.SubscriptionEventResource;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class SubscriptionResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private SubscriptionResource resource;

    @Autowired
    private SubscriptionEventResource subscriptionEventResource;

    @Autowired
    private CqlSession session;

    private String email;

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

        email = UUID.randomUUID() + "@gmail.com";

        Map<String, Object> model = new HashMap<>();
        model.put(SubscriptionField.EMAIL.field, email);

        resource.save(JsonNodeUtils.create(model));

        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        assertThat(event.getEventType(), is(EventType.CREATE));
        assertThat(event.getLocalDate(), is(ServiceContextExecution.context().getDate().toLocalDate()));
        assertThat(event.getData().get("email").textValue(), is(email));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all().size(), is(1));

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode subscription = resource.get(email).get();
        assertThat(subscription.get(SubscriptionField.EMAIL.field).textValue(), is(email));

    }

    @Test(dependsOnMethods = "get")
    public void delete() {

        resource.delete(email);
        assertThat(resource.get(email).isPresent(), is(false));

        SubscriptionEvent event = subscriptionEventResource.getByIdAndDate(email, ServiceContextExecution.context().getDate().toInstant());
        assertThat(event.getEventType(), is(EventType.DELETE));
        assertThat(event.getLocalDate(), is(ServiceContextExecution.context().getDate().toLocalDate()));
        assertThat(event.getData(), is(nullValue()));

        ResultSet countAccountEvents = session.execute(QueryBuilder.selectFrom("test", "subscription_event").all().whereColumn("local_date")
                .isEqualTo(QueryBuilder.literal(ServiceContextExecution.context().getDate().toLocalDate())).build());
        assertThat(countAccountEvents.all().size(), is(2));

    }

}
