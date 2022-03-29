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

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.common.event.EventType;
import com.exemple.service.customer.common.event.ResourceEventPublisher;
import com.exemple.service.customer.core.CustomerTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringJUnitConfig(CustomerTestConfiguration.class)
public class AccountServiceTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountService service;

    @Autowired
    private AccountResource resource;

    @Autowired
    private ResourceEventPublisher publisher;

    @BeforeEach
    private void before() {

        Mockito.reset(resource, publisher);

    }

    @BeforeAll
    public static void initServiceContextExecution() {

        ServiceContext context = ServiceContextExecution.context();
        context.setApp("default");
    }

    @Test
    @DisplayName("save account")
    public void save() throws IOException {

        // Given account

        JsonNode source = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\", \"lastname\": \"Dupont\", \"firstname\":\"Jean\"}");

        // When perform save

        JsonNode account = service.save(source);

        assertThat(account).isNotNull();
        assertAll(
                () -> assertThat(account).hasSize(5),
                () -> assertThat(account.get("email")).hasToString("\"jean.dupont@gmail.com\""),
                () -> assertThat(account.get("lastname")).hasToString("\"Dupont\""),
                () -> assertThat(account.get("firstname")).hasToString("\"Jean\""),
                () -> assertThat(account.get("creation_date")).hasToString("\"" + ServiceContextExecution.context().getDate() + "\""),
                () -> assertThat(account.get("id").isTextual()).isTrue());

        // And check save resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(resource).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue()).isEqualTo(account);

        // And check publish resource

        ArgumentCaptor<JsonNode> eventCaptor = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(publisher).publish(eventCaptor.capture(), Mockito.eq("account"), Mockito.eq(EventType.CREATE));
        assertThat(accountCaptor.getValue()).isEqualTo(account);

    }

    @Test
    @DisplayName("update account")
    public void update() throws IOException {

        // Given account

        JsonNode source = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\", \"lastname\": \"Dupond\"}");

        // And previousAccount

        JsonNode previousSource = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\", \"lastname\": \"Dupont\", \"firstname\":\"Jean\"}");

        // When perform save

        JsonNode account = service.save(source, previousSource);

        // Then check account

        assertAll(
                () -> assertThat(account).isNotNull(),
                () -> assertThat(account).isEqualTo(MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\", \"lastname\": \"Dupond\"}")));

        // And check save resource

        ArgumentCaptor<JsonNode> accountCaptor = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> previousAccountCaptor = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(resource).save(accountCaptor.capture(), previousAccountCaptor.capture());
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
    public void get() throws IOException {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock resource

        JsonNode source = MAPPER.readTree("{\"email\": \"jean.dupont@gmail.com\", \"lastname\": \"Dupont\", \"firstname\":\"Jean\"}");
        Mockito.when(resource.get(Mockito.eq(id))).thenReturn(Optional.of(source));

        // When perform get

        Optional<JsonNode> account = service.get(id);

        // Then check account

        assertThat(account).hasValue(source);

    }

}
