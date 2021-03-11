package com.exemple.service.customer.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class LoginServiceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    @Autowired
    private LoginService service;

    @BeforeMethod
    private void before() {

        Mockito.reset(resource);

    }

    @Test
    public void exist() {

        String login = "jean.dupond@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.init()));

        boolean exist = service.exist(login);

        assertThat(exist, is(Boolean.TRUE));

        Mockito.verify(resource).get(Mockito.eq(login));

    }

    @Test
    public void create() throws LoginServiceException, LoginResourceExistException {

        Mockito.doNothing().when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put("login", "jean@gmail.com");
        model.put("password", "jean.dupont");

        service.save(JsonNodeUtils.create(model));

        Mockito.verify(resource).save(Mockito.any(JsonNode.class));

    }

    @Test(expectedExceptions = LoginServiceAlreadyExistException.class)
    public void createAlreadyExist() throws LoginResourceExistException, LoginServiceAlreadyExistException {

        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put("login", "jean@gmail.com");
        model.put("password", "jean.dupont");

        service.save(JsonNodeUtils.create(model));

    }

    @Test
    public void updatePassword() throws LoginServiceAlreadyExistException {

        String login = "jean@gmail.com";

        Map<String, Object> previousSource = new HashMap<>();
        previousSource.put("password", "jean.dupont");
        previousSource.put(LoginField.USERNAME.field, login);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(previousSource)));

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupond");
        source.put(LoginField.USERNAME.field, login);

        service.save(login, JsonNodeUtils.create(source), JsonNodeUtils.create(previousSource));

        Mockito.verify(resource).update(Mockito.any(JsonNode.class));

    }

    @Test
    public void updateUsername() throws LoginResourceExistException, LoginServiceAlreadyExistException {

        String login = "jean@gmail.com";
        String newLogin = "jack@gmail.com";

        Map<String, Object> previousSource = new HashMap<>();
        previousSource.put("password", "jean.dupont");
        previousSource.put(LoginField.USERNAME.field, login);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(previousSource)));
        Mockito.doNothing().when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put(LoginField.USERNAME.field, newLogin);

        service.save(login, JsonNodeUtils.create(source), JsonNodeUtils.create(previousSource));

        Mockito.verify(resource).save(Mockito.any(JsonNode.class));
        Mockito.verify(resource).delete(login);

    }

    @Test(expectedExceptions = LoginServiceAlreadyExistException.class)
    public void updateAlreadyExist() throws LoginServiceException, LoginResourceExistException {

        String login = "jean@gmail.com";
        String newLogin = "jack@gmail.com";

        Map<String, Object> previousSource = new HashMap<>();
        previousSource.put("password", "jean.dupond");
        previousSource.put(LoginField.USERNAME.field, login);

        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupond");
        source.put(LoginField.USERNAME.field, newLogin);

        service.save(login, JsonNodeUtils.create(source), JsonNodeUtils.create(previousSource));

    }

    @Test
    public void get() throws LoginServiceNotFoundException {

        String login = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.init()));

        JsonNode data = service.get(login);

        assertThat(data, is(notNullValue()));

        Mockito.verify(resource).get(Mockito.eq(login));

    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void getNotFound() throws LoginServiceNotFoundException {

        String login = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.empty());

        service.get(login);

    }

    @Test
    public void getById() throws LoginServiceNotFoundException {

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Arrays.asList(JsonNodeUtils.init(), JsonNodeUtils.init()));

        List<JsonNode> logins = service.get(id);

        assertThat(logins.size(), is(2));

        Mockito.verify(resource).get(Mockito.eq(id));

    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void getByIdNotFound() throws LoginServiceNotFoundException {

        UUID id = UUID.randomUUID();

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Collections.emptyList());

        service.get(id);

    }

    @Test
    public void delete() {

        String login = "jean.dupond@gmail.com";

        Mockito.doNothing().when(resource).delete(Mockito.eq(login));

        service.delete(login);

        Mockito.verify(resource).delete(Mockito.eq(login));

    }
}
