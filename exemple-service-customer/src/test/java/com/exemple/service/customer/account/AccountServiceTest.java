package com.exemple.service.customer.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

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

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@SpringJUnitConfig(CustomerTestConfiguration.class)
class AccountServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource accountResource;

    @Autowired
    private ResourceEventPublisher publisher;

    @BeforeEach
    public void before() {

        Mockito.reset(accountResource, publisher);

    }

    @BeforeAll
    public static void initServiceContextExecution() {

        ServiceContextExecution.setApp("default");
    }

    @Test
    @DisplayName("save account")
    void save() throws IOException {

        // Given account

        JsonNode source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname":"Jean"}
                """);

        // When perform save

        ObjectNode account = service.save(source).deepCopy();

        // Then check account

        assertAll(
                () -> assertThat(account).isNotNull(),
                () -> assertThat(account.get("id").isTextual()).isTrue(),
                () -> assertThat(account.deepCopy().putNull("id")).isEqualTo(MAPPER.readTree(
                        """
                        {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname": "Jean", "creation_date": "%s", "id": null}
                        """.formatted(ServiceContextExecution.context().getDate()))));

        // And check save account resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(accountResource).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isEqualTo(account);

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("account"), Mockito.eq(EventType.CREATE));
        assertThat(accountCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("update account")
    void update() throws IOException {

        // Given account

        JsonNode source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupond"}
                """);

        // And previousAccount

        JsonNode previousSource = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname":"Jean"}
                """);

        // When perform save

        JsonNode account = service.save(source, previousSource);

        // Then check account

        assertAll(
                () -> assertThat(account).isNotNull(),
                () -> assertThat(account).isEqualTo(MAPPER.readTree(
                        """
                        {"email": "jean.dupont@gmail.com", "lastname": "Dupond"}
                        """)));

        // And check save resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> previousAccountCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(accountResource).save(accountCaptor.capture(), previousAccountCaptor.capture());
        assertAll(
                () -> assertThat(accountCaptor.getValue()).isEqualTo(account),
                () -> assertThat(previousAccountCaptor.getValue()).isEqualTo(previousSource));

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("account"), Mockito.eq(EventType.UPDATE));
        assertThat(eventCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("update email")
    void updateEmail() throws IOException {

        // Given account

        UUID id = UUID.randomUUID();

        JsonNode source = MAPPER.readTree(
                """
                {"id": "%s", "email": "jean.dupond@gmail.com"}
                """.formatted(id));

        // And previousAccount

        JsonNode previousSource = MAPPER.readTree(
                """
                {"id": "%s", "email": "jean.dupont@gmail.com"}
                """.formatted(id));

        // When perform save

        JsonNode account = service.save(source, previousSource);

        // Then check account

        assertAll(
                () -> assertThat(account).isNotNull(),
                () -> assertThat(account).isEqualTo(MAPPER.readTree(
                        """
                        {"id": "%s", "email": "jean.dupond@gmail.com"}
                        """.formatted(id))));

        // And check save resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> previousAccountCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(accountResource).save(accountCaptor.capture(), previousAccountCaptor.capture());
        assertAll(
                () -> assertThat(accountCaptor.getValue()).isEqualTo(account),
                () -> assertThat(previousAccountCaptor.getValue()).isEqualTo(previousSource));

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("account"), Mockito.eq(EventType.UPDATE));
        assertThat(eventCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("get account")
    void get() throws IOException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock resource

        JsonNode source = MAPPER.readTree(
                """
                {"email": "jean.dupont@gmail.com", "lastname": "Dupont", "firstname":"Jean"}
                """);
        Mockito.when(accountResource.get(id)).thenReturn(Optional.of(source));

        // When perform get

        Optional<JsonNode> account = service.get(id);

        // Then check account

        assertThat(account).hasValue(source);

    }

}
