package com.exemple.service.customer.account;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.customer.account.model.Account;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class AccountServiceTest extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource resource;

    @Autowired
    private SchemaFilter schemaFilter;

    @Autowired
    private SchemaResource schemaResource;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource, schemaFilter);

    }

    @AfterClass
    private void afterClass() {

        Mockito.reset(schemaResource);

    }

    @Test
    public void save() throws AccountServiceException {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setLastname("Dupont");
        model.setFirstname("Jean");
        model.setOptinEmail(true);
        model.setCivility("Mr");

        Mockito.when(resource.save(Mockito.any(JsonNode.class))).thenReturn(UUID.randomUUID());
        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(model));

        JsonNode account = service.save(JsonNodeUtils.create(model));
        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));
        assertThat(account, hasJsonField("opt_in_email", true));
        assertThat(account, hasJsonField("civility", "Mr"));

        Mockito.verify(resource).save(Mockito.any(JsonNode.class));

    }

    @Test
    public void update() throws AccountServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(JsonNodeUtils.create(model)));
        Mockito.doNothing().when(resource).save(Mockito.eq(id), Mockito.any(JsonNode.class), Mockito.any(JsonNode.class));
        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(model));

        ArrayNode patch = MAPPER.createArrayNode();

        ObjectNode addLastName = MAPPER.createObjectNode();
        addLastName.put("op", "add");
        addLastName.put("path", "/lastname");
        addLastName.put("value", "Dupond");
        patch.add(addLastName);

        JsonNode account = service.save(id, patch);

        assertThat(account, is(notNullValue()));
        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));

        Mockito.verify(resource).get(Mockito.eq(id));
        Mockito.verify(resource).save(Mockito.eq(id), Mockito.any(JsonNode.class), Mockito.any(JsonNode.class));

    }

    @Test
    public void get() throws AccountServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("email", "jean.dupont@gmail.com");
        model.put("lastname", "Dupont");
        model.put("firstname", "Jean");

        UUID id = UUID.randomUUID();

        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(model));
        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(JsonNodeUtils.create(model)));

        JsonNode account = service.get(id);
        assertThat(account, is(notNullValue()));

        assertThat(account, hasJsonField("email", "jean.dupont@gmail.com"));
        assertThat(account, hasJsonField("lastname", "Dupont"));
        assertThat(account, hasJsonField("firstname", "Jean"));

        Mockito.verify(resource).get(Mockito.eq(id));

    }

    @Test(expectedExceptions = AccountServiceNotFoundException.class)
    public void getNotFound() throws AccountServiceException {

        Account model = new Account();
        model.setEmail("jean.dupont@gmail.com");
        model.setLastname("Dupont");

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.empty());

        service.get(id);

        Mockito.verify(resource).get(Mockito.eq(id));

    }

}
