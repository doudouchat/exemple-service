package com.exemple.service.customer.login;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
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
import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class LoginServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    @Autowired
    private LoginService service;

    @Autowired
    private SchemaFilter schemaFilter;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource, schemaFilter);

    }

    @Test
    public void exist() {

        String login = "jean.dupond@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.init()));

        service.exist(login);

        assertThat(service.exist(login), is(Boolean.TRUE));

    }

    @Test
    public void create() throws LoginServiceException, LoginResourceExistException {

        Mockito.doNothing().when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put("login", "jean@gmail.com");
        model.put("password", "jean.dupont");

        service.save(JsonNodeUtils.create(model));

    }

    @Test(expectedExceptions = LoginServiceException.class)
    public void createAlreadyExist() throws LoginServiceException, LoginResourceExistException {

        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put("login", "jean@gmail.com");
        model.put("password", "jean.dupont");

        service.save(JsonNodeUtils.create(model));

    }

    @DataProvider(name = "update")
    private static Object[][] update() {

        return new Object[][] {

                { true },

                { false }

        };
    }

    @Test(dataProvider = "update")
    public void update(boolean created) throws LoginServiceException, LoginResourceExistException {

        String login = "jean@gmail.com";

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put(LoginField.USERNAME.field, login);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(source)));
        Mockito.when(resource.save(Mockito.eq(login), Mockito.any(JsonNode.class))).thenReturn(created);

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");
        service.save(login, JsonNodeUtils.create(model));

    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void updateNotFound() throws LoginServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");

        String login = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.empty());

        service.save(login, JsonNodeUtils.create(model));

    }

    @Test(expectedExceptions = LoginServiceException.class)
    public void updateAlreadyExist() throws LoginServiceException, LoginResourceExistException {

        String login = "jean@gmail.com";

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put(LoginField.USERNAME.field, login);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(source)));
        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.anyString(), Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put(LoginField.USERNAME.field, "jack.dupont");
        service.save(login, JsonNodeUtils.create(model));

    }

    @Test
    public void get() throws LoginServiceNotFoundException {

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");

        String login = "jean@gmail.com";

        Mockito.when(schemaFilter.filter(Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(JsonNode.class))).thenReturn(JsonNodeUtils.create(model));
        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(model)));

        JsonNode data = service.get(login);

        assertThat(data, is(notNullValue()));
        assertThat(data, hasJsonField("password", (String) model.get("password")));

    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void getNotFound() throws LoginServiceNotFoundException {

        String login = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.empty());

        service.get(login);

    }

    @Test
    public void delete() {

        String login = "jean.dupond@gmail.com";

        Mockito.doNothing().when(resource).delete(Mockito.eq(login));

        service.delete(login);

    }
}
