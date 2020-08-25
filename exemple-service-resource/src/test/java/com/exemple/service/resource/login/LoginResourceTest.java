package com.exemple.service.resource.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class LoginResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    private Map<String, Object> source;

    private String username;

    private UUID id;

    @Test
    public void save() throws LoginResourceExistException {

        username = UUID.randomUUID() + "@gmail.com";
        id = UUID.randomUUID();

        source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put("id", id);
        source.put("username", username);
        source.put("enable", true);

        resource.save(JsonNodeUtils.create(source));

    }

    @Test(dependsOnMethods = "save", expectedExceptions = LoginResourceExistException.class)
    public void saveFailure() throws LoginResourceExistException {

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put("id", UUID.randomUUID());
        source.put("username", username);

        try {
            resource.save(JsonNodeUtils.create(source));
        } catch (LoginResourceExistException e) {
            assertThat(e.getLogin(), is(username));
            throw e;
        }

    }

    @Test(dependsOnMethods = "save")
    public void get() {

        JsonNode login0 = resource.get(username).get();
        assertThat(login0.get(LoginField.USERNAME.field).textValue(), is(username));
        assertThat(login0.get(LoginField.ID.field).textValue(), is(source.get("id").toString()));
        assertThat(login0.get("password").textValue(), is(source.get("password")));
        assertThat(login0.get("enable").booleanValue(), is(source.get("enable")));
        assertThat(login0.path("note").getNodeType(), is(JsonNodeType.MISSING));
    }

    @Test(dependsOnMethods = "save")
    public void getById() throws LoginResourceExistException {

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put("id", id);
        source.put("username", UUID.randomUUID() + "@gmail.com");
        source.put("enable", true);

        resource.save(JsonNodeUtils.create(source));

        List<JsonNode> logins = resource.get(id);
        assertThat(logins.size(), is(2));

        JsonNode login1 = logins.stream().filter(l -> username.equals(l.get(LoginField.USERNAME.field).textValue())).findFirst().get();
        assertThat(login1, is(notNullValue()));
        assertThat(login1.get(LoginField.ID.field).textValue(), is(id.toString()));
        assertThat(login1.get("password").textValue(), is(this.source.get("password")));
        assertThat(login1.get("enable").booleanValue(), is(this.source.get("enable")));
        assertThat(login1.path("note").getNodeType(), is(JsonNodeType.MISSING));

        JsonNode login2 = logins.stream().filter(l -> source.get("username").equals(l.get(LoginField.USERNAME.field).textValue())).findFirst().get();
        assertThat(login2, is(notNullValue()));
        assertThat(login2.get(LoginField.ID.field).textValue(), is(id.toString()));
        assertThat(login2.get("password").textValue(), is(source.get("password")));
        assertThat(login2.get("enable").booleanValue(), is(source.get("enable")));
        assertThat(login2.path("note").getNodeType(), is(JsonNodeType.MISSING));
    }

    @Test(dependsOnMethods = { "get", "getById" })
    public void update() {

        Map<String, Object> source = new HashMap<>();
        source.put("password", "jean.dupont");
        source.put("id", UUID.randomUUID());
        source.put("username", username);

        resource.update(JsonNodeUtils.create(source));

        JsonNode login = resource.get(username).get();
        assertThat(login.get(LoginField.USERNAME.field).textValue(), is(username));
        assertThat(login.get(LoginField.ID.field).textValue(), is(source.get("id").toString()));
        assertThat(login.get("password").textValue(), is(source.get("password")));
        assertThat(login.path("enable").getNodeType(), is(JsonNodeType.MISSING));
        assertThat(login.path("note").getNodeType(), is(JsonNodeType.MISSING));
    }

    @Test(dependsOnMethods = "update")
    public void delete() {

        resource.delete(username);
        assertThat(resource.get(username).isPresent(), is(false));
    }

}
