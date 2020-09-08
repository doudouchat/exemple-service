package com.exemple.service.customer.login;

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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.exemple.service.customer.login.exception.LoginServiceException;
import com.exemple.service.customer.login.exception.LoginServiceNotFoundException;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ContextConfiguration(classes = { CustomerTestConfiguration.class })
public class LoginServiceTest extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    @Test
    public void createAlreadyExist() throws LoginServiceException, LoginResourceExistException {

        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put("login", "jean@gmail.com");
        model.put("password", "jean.dupont");

        try {
            service.save(JsonNodeUtils.create(model));

            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            assertThat(e.getAllExceptions().size(), is(1));

            ValidationExceptionModel exception = e.getAllExceptions().stream().findFirst().get();

            assertThat(exception.getCode(), is("login"));
            assertThat(exception.getPath(), is("/username"));

            Mockito.verify(resource).save(Mockito.any(JsonNode.class));

        }

    }

    @Test
    public void updatePassword() throws LoginServiceNotFoundException {

        String login = "jean@gmail.com";

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put(LoginField.USERNAME.field, login);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(source)));

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");

        ArrayNode patch = MAPPER.createArrayNode();

        ObjectNode replacePassword = MAPPER.createObjectNode();
        replacePassword.put("op", "replace");
        replacePassword.put("path", "/password");
        replacePassword.put("value", "jean.dupont");
        patch.add(replacePassword);

        service.save(login, patch);

        Mockito.verify(resource).update(Mockito.any(JsonNode.class));

    }

    @Test
    public void updateUsername() throws LoginServiceException, LoginResourceExistException {

        String login = "jean@gmail.com";
        String newLogin = "jack@gmail.com";

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put(LoginField.USERNAME.field, newLogin);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(source)));
        Mockito.doNothing().when(resource).save(Mockito.any(JsonNode.class));

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");

        ArrayNode patch = MAPPER.createArrayNode();

        ObjectNode replaceLogin = MAPPER.createObjectNode();
        replaceLogin.put("op", "replace");
        replaceLogin.put("path", "/" + LoginField.USERNAME.field);
        replaceLogin.put("value", newLogin);
        patch.add(replaceLogin);

        service.save(login, patch);

        Mockito.verify(resource).save(Mockito.any(JsonNode.class));
        Mockito.verify(resource).delete(login);

    }

    @Test(expectedExceptions = LoginServiceNotFoundException.class)
    public void updateNotFound() throws LoginServiceException {

        Map<String, Object> model = new HashMap<>();
        model.put("password", "jean.dupont");

        String login = "jean@gmail.com";

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.empty());

        service.save(login, MAPPER.createArrayNode());

    }

    @Test
    public void updateAlreadyExist() throws LoginServiceException, LoginResourceExistException {

        String login = "jean@gmail.com";

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put(LoginField.USERNAME.field, login);

        Mockito.when(resource.get(Mockito.eq(login))).thenReturn(Optional.of(JsonNodeUtils.create(source)));
        Mockito.doThrow(new LoginResourceExistException("jean@gmail.com")).when(resource).save(Mockito.any(JsonNode.class));

        ObjectNode replaceEmail = MAPPER.createObjectNode();
        replaceEmail.put("op", "replace");
        replaceEmail.put("path", "/" + LoginField.USERNAME.field);
        replaceEmail.put("value", "jack.dupont");

        ArrayNode patch = MAPPER.createArrayNode();
        patch.add(replaceEmail);

        try {
            service.save(login, patch);
            Assert.fail("expected ValidationException");

        } catch (ValidationException e) {

            assertThat(e.getAllExceptions().size(), is(1));

            ValidationExceptionModel exception = e.getAllExceptions().stream().findFirst().get();

            assertThat(exception.getCode(), is("login"));
            assertThat(exception.getPath(), is("/username"));

            Mockito.verify(resource).save(Mockito.any(JsonNode.class));
        }

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
    public void delete() {

        String login = "jean.dupond@gmail.com";

        Mockito.doNothing().when(resource).delete(Mockito.eq(login));

        service.delete(login);

        Mockito.verify(resource).delete(Mockito.eq(login));

    }
}
