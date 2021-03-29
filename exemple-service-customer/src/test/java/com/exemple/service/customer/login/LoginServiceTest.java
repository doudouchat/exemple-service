package com.exemple.service.customer.login;

import static nl.fd.hamcrest.jackson.HasJsonField.hasJsonField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.ArrayList;
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
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.JsonNodeUtils;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.login.exception.LoginServiceAlreadyExistException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

    @BeforeClass
    private void initServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
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
    public void updatePassword() {

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

        service.save(source, previousSource);

        // Then check save resource

        ArgumentCaptor<JsonNode> loginCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).update(loginCaptor.capture());
        assertThat(loginCaptor.getValue(), hasJsonField("username", username));
        assertThat(loginCaptor.getValue().get("password").textValue(), startsWith("{bcrypt}"));

    }

    @Test
    public void updateUsername() throws LoginResourceExistException, LoginServiceAlreadyExistException {

        // When perform save

        ArrayNode previousSource = JsonNodeUtils.toArrayNode(() -> {

            Map<String, Object> login1 = new HashMap<>();
            login1.put("username", "jean1@gmail.com");
            login1.put("password", "mdp123");

            Map<String, Object> login2 = new HashMap<>();
            login2.put("username", "jean2@gmail.com");
            login2.put("password", "mdp123");

            List<Object> logins = new ArrayList<>();
            logins.add(login1);
            logins.add(login2);

            return logins;

        });

        ArrayNode source = JsonNodeUtils.toArrayNode(() -> {

            Map<String, Object> login1 = new HashMap<>();
            login1.put("username", "jean1@gmail.com");
            login1.put("password", "mdp124");

            Map<String, Object> login2 = new HashMap<>();
            login2.put("username", "jean3@gmail.com");
            login2.put("password", "mdp124");

            List<Object> logins = new ArrayList<>();
            logins.add(login1);
            logins.add(login2);

            return logins;

        });

        service.save(source, previousSource);

        // Then check save resource

        ArgumentCaptor<JsonNode> loginCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).save(loginCaptor.capture());
        assertThat(loginCaptor.getValue(), hasJsonField("username", "jean3@gmail.com"));
        assertThat(loginCaptor.getValue().get("password").textValue(), startsWith("{bcrypt}"));

        // And check update resource

        Mockito.verify(resource).update(loginCaptor.capture());
        assertThat(loginCaptor.getValue(), hasJsonField("username", "jean1@gmail.com"));
        assertThat(loginCaptor.getValue().get("password").textValue(), startsWith("{bcrypt}"));

        // And check delete resource
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(resource).delete(usernameCaptor.capture());
        assertThat(usernameCaptor.getValue(), is("jean2@gmail.com"));

    }

    @Test
    public void updateUsernameFailureUsernameAlreadyExist() throws LoginResourceExistException {

        // Given mock exist

        JsonNode login = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("username", "jean2@gmail.com");
            model.put("password", "mdp123");

            return model;

        });

        Mockito.when(resource.get(Mockito.eq("jean2@gmail.com"))).thenReturn(Optional.of(login));

        // When perform save

        ArrayNode previousSource = JsonNodeUtils.toArrayNode(() -> new ArrayList<>());

        ArrayNode source = JsonNodeUtils.toArrayNode(() -> {

            Map<String, Object> login1 = new HashMap<>();
            login1.put("username", "jean1@gmail.com");
            login1.put("password", "mdp123");

            Map<String, Object> login2 = new HashMap<>();
            login2.put("username", "jean2@gmail.com");
            login2.put("password", "mdp123");

            List<Object> logins = new ArrayList<>();
            logins.add(login1);
            logins.add(login2);

            return logins;

        });

        try {

            service.save(source, previousSource);

            Assert.fail("LoginServiceAlreadyExistException must be throwed");

        } catch (LoginServiceAlreadyExistException e) {

            // Then
            assertThat(e.getUsername(), is("jean2@gmail.com"));

            // And check save resource
            Mockito.verify(resource, Mockito.never()).save(Mockito.any());

            // And check update resource

            Mockito.verify(resource, Mockito.never()).update(Mockito.any());

            // And check delete resource
            Mockito.verify(resource, Mockito.never()).delete(Mockito.any());
        }

    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Username must be equals")
    public void updateUsernameFailure() {

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

        service.save(source, previousSource);

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

        ArrayNode logins = service.get(id);

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
