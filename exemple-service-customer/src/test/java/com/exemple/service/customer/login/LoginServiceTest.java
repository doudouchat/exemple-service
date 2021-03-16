package com.exemple.service.customer.login;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.customer.common.JsonNodeUtils;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
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

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // And mock service

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "mdp123");

            return model;

        });

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.of(source));

        // When perform exist

        boolean exist = service.exist(username);

        // Then check exist

        assertThat(exist, is(Boolean.TRUE));

    }

    @Test
    public void create() throws LoginServiceAlreadyExistException, LoginResourceExistException {

        // Given source

        JsonNode login = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", "jean@gmail.com");
            model.put("password", "jean.dupont");

            return model;

        });

        // When perform save

        service.save(login);

        // Then check save resource

        ArgumentCaptor<JsonNode> loginCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).save(loginCaptor.capture());
        assertThat(loginCaptor.getValue(), hasJsonField("username", "jean@gmail.com"));
        assertThat(loginCaptor.getValue().get("password").textValue(), startsWith("{bcrypt}"));

    }

    @Test(expectedExceptions = LoginServiceAlreadyExistException.class)
    public void createAlreadyExist() throws LoginResourceExistException, LoginServiceAlreadyExistException {

        // Given source

        JsonNode login = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", "jean@gmail.com");
            return model;

        });

        // And mock resource

        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        // When perform save

        service.save(login);

    }

    @Test
    public void updatePassword() throws LoginServiceAlreadyExistException {

        // Given user_name

        String username = "jean@gmail.com";

        // When perform save

        JsonNode previousSource = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "mdp123");

            return model;

        });

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "mdp124");

            return model;

        });

        service.save(username, source, previousSource);

        // Then check save resource

        ArgumentCaptor<JsonNode> loginCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).update(loginCaptor.capture());
        assertThat(loginCaptor.getValue(), hasJsonField("username", username));
        assertThat(loginCaptor.getValue().get("password").textValue(), startsWith("{bcrypt}"));

    }

    @Test
    public void updateUsername() throws LoginResourceExistException, LoginServiceAlreadyExistException {

        // Given user_name

        String username = "jean@gmail.com";
        String newUsername = "jack@gmail.com";

        // When perform save

        JsonNode previousSource = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "mdp123");

            return model;

        });

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", newUsername);
            model.put("password", "mdp123");

            return model;

        });

        service.save(username, source, previousSource);

        // Then check save resource

        ArgumentCaptor<JsonNode> loginCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).save(loginCaptor.capture());
        assertThat(loginCaptor.getValue(), is(source));

        // And check delete resource

        Mockito.verify(resource).delete(username);

    }

    @Test(expectedExceptions = LoginServiceAlreadyExistException.class)
    public void updateAlreadyExist() throws LoginServiceException, LoginResourceExistException {

        // Given user_name

        String username = "jean@gmail.com";
        String newUsername = "jack@gmail.com";

        // And mock resource

        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        // When perform save

        JsonNode previousSource = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "mdp123");

            return model;

        });

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", newUsername);
            model.put("password", "mdp123");

            return model;

        });

        service.save(username, source, previousSource);

    }

    @Test
    public void get() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean@gmail.com";

        // And mock resource

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("password", "mdp123");

            return model;

        });

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.of(source));

        // When perform get

        JsonNode login = service.get(username);

        // Then check login

        assertThat(login, is(source));

    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void getNotFound() throws LoginServiceNotFoundException {

        // Given user_name

        String username = "jean@gmail.com";

        // And mock resource

        Mockito.when(resource.get(Mockito.eq(username))).thenReturn(Optional.empty());

        // When perform get

        service.get(username);

    }

    @Test
    public void getById() throws LoginServiceNotFoundException {

        // Given id

        UUID id = UUID.randomUUID();

        // And mock resource

        JsonNode source1 = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("id", id);
            model.put("username", "jean.dupond");
            model.put("password", "mdp123");

            return model;

        });

        JsonNode source2 = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("id", id);
            model.put("username", "jean.dupont");
            model.put("password", "mdp123");

            return model;

        });

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Arrays.asList(source1, source2));

        // When perform get

        List<JsonNode> logins = service.get(id);

        // Then check logins

        assertThat(logins.size(), is(2));
        assertThat(logins.get(0), is(source1));
        assertThat(logins.get(1), is(source2));
    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void getByIdNotFound() throws LoginServiceNotFoundException {

        // Given id

        UUID id = UUID.randomUUID();

        // And mock resource

        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Collections.emptyList());

        // When perform get

        service.get(id);

    }

    @Test
    public void delete() {

        // Given user_name

        String username = "jean.dupond@gmail.com";

        // When perform delete

        service.delete(username);

        // Then check delete resource

        Mockito.verify(resource).delete(username);

    }
}
