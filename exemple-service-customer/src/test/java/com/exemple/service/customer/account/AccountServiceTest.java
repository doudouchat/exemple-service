package com.exemple.service.customer.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExtension;
import com.exemple.service.context.WithServiceContext;
import com.exemple.service.customer.core.CustomerTestConfiguration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringJUnitConfig(CustomerTestConfiguration.class)
@ExtendWith(ServiceContextExtension.class)
class AccountServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource accountResource;

    @BeforeEach
    void before() {

        Mockito.reset(accountResource);

    }

    @Test
    @WithServiceContext
    @DisplayName("create account")
    void create() {

        // Given account

        var source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname":"Jean"}
                """);

        // When perform save

        var account = service.create(source);
        var id = account.get("id");

        // Then check account

        assertThat(account).isEqualTo(MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname": "Jean", "creation_date": "%s", "id": "%s"}
                """.formatted(ServiceContext.SERVICE_CONTEXT.get().date(), id.stringValue())));

        // And check save account resource

        var accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(accountResource).create(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("update account")
    void update() {

        // Given account

        var source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupond"}
                """);

        // When perform save

        var account = service.update(source);

        // Then check account

        assertThat(account).isEqualTo(MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupond"}
                """));

        // And check save resource

        var accountCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(accountResource).update(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("update email")
    void updateEmail() {

        // Given account

        var id = UUID.randomUUID();

        var source = MAPPER.readTree(
                """
                {"id": "%s", "email": "jean.dupond@gmail.com"}
                """.formatted(id));

        // When perform save

        var account = service.update(source);

        // Then check account

        assertThat(account).isEqualTo(MAPPER.readTree(
                """
                {"id": "%s", "email": "jean.dupond@gmail.com"}
                """.formatted(id)));

        // And check save resource

        var accountCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(accountResource).update(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("get account")
    void get() {

        // Given account id

        var id = UUID.randomUUID();

        // And mock resource

        var source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname":"Jean"}
                """);
        Mockito.when(accountResource.get(id)).thenReturn(Optional.of(source));

        // When perform get

        var account = service.get(id);

        // Then check account

        assertThat(account).hasValue(source);

    }

}
