package com.exemple.service.api.account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.api.common.JsonNodeUtils;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.schema.merge.SchemaMerge;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class AccountApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private AccountService service;

    @Autowired
    private SchemaValidation schemaValidation;

    @Autowired
    private SchemaMerge schemaMerge;

    @Autowired
    private JsonNode account;

    @BeforeMethod
    private void before() {

        Mockito.reset(service, schemaValidation, schemaMerge);

    }

    public static final String URL = "/v1/accounts";

    @Test
    public void get() {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(Optional.of(account));

        // When perform get

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(account));

    }

    @Test
    public void patch() {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service

        Mockito.when(service.get(Mockito.eq(id))).thenReturn(Optional.of(account));

        // When perform patch

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/birthday");
        patch.put("value", "1976-12-12");

        Response response = target(URL + "/" + id).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .method("PATCH", Entity.json(JsonNodeUtils.toString(Collections.singletonList(patch))));

        // Then check status

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        // And check service

        ArgumentCaptor<JsonNode> previousAccount = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);

        JsonNode expectedAccount = JsonNodeUtils.set(this.account, "birthday", new TextNode("1976-12-12"));

        Mockito.verify(service).save(account.capture(), previousAccount.capture());
        assertThat(previousAccount.getValue(), is(this.account));
        assertThat(account.getValue(), is(expectedAccount));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"), account.capture(),
                previousAccount.capture());
        assertThat(previousAccount.getValue(), is(this.account));
        assertThat(account.getValue(), is(expectedAccount));

    }

    @Test
    public void put() {

        // Given account id

        UUID id = UUID.randomUUID();

        // And mock service
        Mockito.when(service.get(Mockito.eq(id))).thenReturn(Optional.of(this.account));

        // When perform put

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("lastname", "Dupond");

            return model;

        });

        Response response = target(URL + "/" + id).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .put(Entity.json(source));

        // Then check status

        assertThat(response.getStatus(), is(Status.NO_CONTENT.getStatusCode()));

        // And check service

        ArgumentCaptor<JsonNode> previousAccount = ArgumentCaptor.forClass(JsonNode.class);
        ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);

        Mockito.verify(service).save(account.capture(), previousAccount.capture());
        assertThat(previousAccount.getValue(), is(this.account));
        assertThat(account.getValue(), is(source));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"), account.capture(),
                previousAccount.capture());
        assertThat(previousAccount.getValue(), is(this.account));
        assertThat(account.getValue(), is(source));

        // And check merge
        Mockito.verify(schemaMerge).mergeMissingFieldFromOriginal(Mockito.eq("test"), Mockito.eq("v1"), Mockito.eq("account"), Mockito.anyString(),
                account.capture(), previousAccount.capture());
        assertThat(previousAccount.getValue(), is(this.account));
        assertThat(account.getValue(), is(source));

    }

    @Test
    public void create() {

        // Given mock service

        Mockito.when(service.save(Mockito.any(JsonNode.class))).thenReturn(this.account);

        // When perform post

        JsonNode source = JsonNodeUtils.create(() -> {

            Map<String, Object> model = new HashMap<>();
            model.put("email", "jean.dupond@gmail.com");
            model.put("lastname", "dupond");
            model.put("firstname", "jean");

            return model;

        });

        Response response = target(URL).request(MediaType.APPLICATION_JSON)

                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")

                .post(Entity.json(source));

        // Then check status

        assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));

        // And check location

        URI baseUri = target(URL).getUri();
        assertThat(response.getLocation(), is(URI.create(baseUri + "/" + this.account.get("id").textValue())));

        // And check service

        ArgumentCaptor<JsonNode> account = ArgumentCaptor.forClass(JsonNode.class);
        Mockito.verify(service).save(account.capture());
        assertThat(account.getValue(), is(source));

        // And check validation

        Mockito.verify(schemaValidation).validate(Mockito.eq("test"), Mockito.eq("v1"), Mockito.anyString(), Mockito.eq("account"),
                account.capture());
        assertThat(account.getValue(), is(source));

    }

}
