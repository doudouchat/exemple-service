package com.exemple.service.resource.login;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.login.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.login.model.LoginEntity;

@ContextConfiguration(classes = { ResourceTestConfiguration.class })
public class LoginResourceTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private LoginResource resource;

    @Test
    public void create() throws UsernameAlreadyExistsException {

        // Given login

        String username = UUID.randomUUID() + "gmail.com";
        UUID id = UUID.randomUUID();

        LoginEntity login = new LoginEntity();
        login.setUsername(username);
        login.setId(id);

        // When perform

        resource.save(login);

        // Then check login

        LoginEntity actualLogin = resource.get(username).get();
        assertThat(actualLogin.getUsername(), is(username));
        assertThat(actualLogin.getId(), is(id));
    }

    @Test(expectedExceptions = UsernameAlreadyExistsException.class)
    public void createFailureIfUsernameAlreadyExists() throws UsernameAlreadyExistsException {

        // Given login

        LoginEntity login = new LoginEntity();
        login.setUsername("jean.dupond@gmail.com");

        // When perform

        resource.save(login);

    }

    @Test
    public void get() {

        // When perform get

        LoginEntity login = resource.get("jean.dupond@gmail.com").get();

        // Then check login

        assertThat(login.getUsername(), is("jean.dupond@gmail.com"));
        assertThat(login.getId(), is(UUID.fromString("e2b0fdc7-bd69-410d-b684-207c8cbf7598")));
    }

    @Test
    public void delete() throws UsernameAlreadyExistsException {

        // Given login

        String username = UUID.randomUUID() + "gmail.com";

        LoginEntity login = new LoginEntity();
        login.setUsername(username);

        resource.save(login);

        // When perform

        resource.delete(username);

        // Then check login

        Optional<LoginEntity> actualLogin = resource.get(username);
        assertThat(actualLogin.isPresent(), is(false));

    }

}
