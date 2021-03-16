package com.exemple.service.customer.account;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.customer.common.JsonNodeUtils;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.account.AccountResource;
import com.fasterxml.jackson.databind.JsonNode;

@RecordApplicationEvents
@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class AccountServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource resource;

    @Autowired
    private ApplicationEvents applicationEvents;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource);

    }

    @BeforeClass
    private void initServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
    }

    @Test
    public void save() {

        // Given mock service

        UUID accountId = UUID.randomUUID();
        Mockito.when(resource.save(Mockito.any(JsonNode.class))).thenReturn(accountId);

        // When perform save

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            model.put("lastname", "Dupont");
            model.put("firstname", "Jean");

            return model;

        });

        JsonNode account = service.save(source);

        // Then check account

        assertThat(account, is(notNullValue()));
        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));

        // And check save resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue(), is(account));

        // And check publish resource

        Optional<EventData> event = applicationEvents.stream(EventData.class).findFirst();
        assertThat(event.isPresent(), is(true));
        assertThat(event.get().getSource(), is(account));
        assertThat(event.get().getEventType(), is(EventType.CREATE));
        assertThat(event.get().getResource(), is("account"));

    }

    @Test
    public void update() {

        // When perform save

        JsonNode previousSource = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            model.put("lastname", "Dupont");
            model.put("firstname", "Jean");

            return model;

        });

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            model.put("lastname", "Dupond");

            return model;

        });

        JsonNode account = service.save(source, previousSource);

        // Then check account

        assertThat(account, is(notNullValue()));
        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupond"));

        // And check save resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> previousAccountCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(accountCaptor.capture(), previousAccountCaptor.capture());
        assertThat(accountCaptor.getValue(), is(account));
        assertThat(previousAccountCaptor.getValue(), is(previousSource));

        // And check publish resource

        Optional<EventData> event = applicationEvents.stream(EventData.class).findFirst();
        assertThat(event.isPresent(), is(true));
        assertThat(event.get().getSource(), is(account));
        assertThat(event.get().getEventType(), is(EventType.UPDATE));
        assertThat(event.get().getResource(), is("account"));

    }

    @Test
    public void get() throws AccountServiceException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock resource

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupont@gmail.com");
            model.put("lastname", "Dupont");
            model.put("firstname", "Jean");

            return model;

        });

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(source));

        // When perform get

        JsonNode account = service.get(id);

        // Then check account

        assertThat(account, is(source));

    }

    @Test(expectedExceptions = AccountServiceNotFoundException.class)
    public void getNotFound() throws AccountServiceException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock resource

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.empty());

        // When perform get

        service.get(id);

    }

}
