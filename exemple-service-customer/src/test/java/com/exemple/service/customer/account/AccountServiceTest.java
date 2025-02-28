package com.exemple.service.customer.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.exemple.service.context.AccountContextExecution;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(CustomerTestConfiguration.class)
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

    @BeforeAll
    static void initServiceContextExecution() {

        ServiceContextExecution.setApp("default");
    }

    @Test
    @DisplayName("create account")
    void create() throws IOException {

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
                """.formatted(ServiceContextExecution.context().getDate(), id.textValue())));

        // And check save account resource

        var accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(accountResource).create(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("update account")
    void update() throws IOException {

        // Given account

        var source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupond"}
                """);

        // And previousAccount

        var previousSource = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname":"Jean"}
                """);
        AccountContextExecution.setPreviousAccount(previousSource);

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
    void updateEmail() throws IOException {

        // Given account

        var id = UUID.randomUUID();

        var source = MAPPER.readTree(
                """
                {"id": "%s", "email": "jean.dupond@gmail.com"}
                """.formatted(id));

        // And previousAccount

        var previousSource = MAPPER.readTree(
                """
                {"id": "%s", "email": "jean.dupont@gmail.com"}
                """.formatted(id));
        AccountContextExecution.setPreviousAccount(previousSource);

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
    void get() throws IOException {

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
