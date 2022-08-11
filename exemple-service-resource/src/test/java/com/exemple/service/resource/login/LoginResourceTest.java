package com.exemple.service.resource.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.customer.login.LoginResource;
import com.exemple.service.customer.login.UsernameAlreadyExistsException;
import com.exemple.service.resource.core.ResourceTestConfiguration;

@SpringJUnitConfig(ResourceTestConfiguration.class)
class LoginResourceTest {

    @Autowired
    private LoginResource resource;

    @Test
    void create() {

        // Given login

        String username = UUID.randomUUID() + "gmail.com";
        UUID id = UUID.randomUUID();

        // When perform

        resource.save(id, username);

        // Then check login

        Optional<UUID> actualLogin = resource.get(username);
        assertThat(actualLogin).hasValue(id);
    }

    @Test
    void createFailureIfUsernameAlreadyExists() {

        // When perform
        Throwable throwable = catchThrowable(() -> resource.save(UUID.randomUUID(), "jean.dupond@gmail.com"));

        // Then check throwable
        assertThat(throwable).isInstanceOf(UsernameAlreadyExistsException.class).hasMessage("[jean.dupond@gmail.com] already exists");

    }

    @Test
    void delete() {

        // Given login

        String username = UUID.randomUUID() + "gmail.com";

        resource.save(UUID.randomUUID(), username);

        // When perform

        resource.delete(username);

        // Then check login

        Optional<UUID> actualLogin = resource.get(username);
        assertThat(actualLogin).isEmpty();

    }

}
