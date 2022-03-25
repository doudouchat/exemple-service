package com.exemple.service.resource.login;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.resource.core.ResourceTestConfiguration;
import com.exemple.service.resource.login.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.login.model.LoginEntity;

@SpringJUnitConfig(ResourceTestConfiguration.class)
public class LoginResourceTest {

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
        assertAll(
                () -> assertThat(actualLogin.getUsername(), is(username)),
                () -> assertThat(actualLogin.getId(), is(id)));
    }

    @Test
    public void createFailureIfUsernameAlreadyExists() {

        // Given login

        LoginEntity login = new LoginEntity();
        login.setUsername("jean.dupond@gmail.com");

        // When perform
        Throwable throwable = catchThrowable(() -> resource.save(login));

        // Then check throwable
        assertThat(throwable, instanceOf(UsernameAlreadyExistsException.class));

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
