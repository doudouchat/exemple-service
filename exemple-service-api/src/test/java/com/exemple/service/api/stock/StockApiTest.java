package com.exemple.service.api.stock;

import static com.exemple.service.api.common.model.ApplicationBeanParam.APP_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.common.NoFoundStockException;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;

public class StockApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private StockService service;

    @BeforeMethod
    private void before() {

        Mockito.reset(service);

    }

    public static final String URL = "/v1/stocks";

    @Test
    public void update() throws InsufficientStockException {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(service.update(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product), Mockito.eq(5))).thenReturn(18L);

        // When perform post

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json("{\"increment\":5}"));

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(Long.class), is(18L));

    }

    @Test
    public void updateValidationFailure() {

        // Given stock

        String store = "store";
        String product = "product";
        String application = "application";

        // When perform post

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json("{}"));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

        // And check body

        assertThat(response.readEntity(String.class), is("{\"increment\":\"La valeur doit être renseignée.\"}"));

    }

    @Test
    public void updateInsufficientStockFailure() throws InsufficientStockException {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.doThrow(new InsufficientStockException("/" + company, "/" + store, "/" + product, 100, 5)).when(service)
                .update(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product), Mockito.eq(5));

        // When perform put

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json("{\"increment\":5}"));

        // Then check status

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

        // And check body

        assertThat(response.readEntity(String.class),
                is(new InsufficientStockException("/" + company, "/" + store, "/" + product, 100, 5).getMessage()));

    }

    @Test
    public void get() throws NoFoundStockException {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(service.get(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product))).thenReturn(5L);

        // When perform service

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // and check body

        JsonNode responseEntity = response.readEntity(JsonNode.class);
        assertThat(responseEntity.get("amount").asLong(), is(5L));
        assertThat(responseEntity.has("increment"), is(false));

    }

    @Test
    public void getNotFoundStockFailure() throws NoFoundStockException {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(service.get(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product)))
                .thenThrow(new NoFoundStockException("/" + store, "/" + product, new RuntimeException()));

        // When perform service

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

}
