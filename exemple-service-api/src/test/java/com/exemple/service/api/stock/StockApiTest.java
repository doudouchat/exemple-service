package com.exemple.service.api.stock;

import static com.exemple.service.api.common.model.ApplicationBeanParam.APP_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.InputStream;
import java.util.Collections;

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
import com.exemple.service.api.stock.model.Stock;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.common.NoFoundStockException;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class StockApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

    private static final String URL = "/v1/stocks";

    @Test
    public void update() throws Exception {

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";
        Stock stock = new Stock();
        stock.setIncrement(5);

        Mockito.when(service.update(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product), Mockito.eq(stock.getIncrement())))
                .thenReturn(18L);

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json(JsonNodeUtils.create(Collections.singletonMap("increment", stock.getIncrement())).toString()));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        Mockito.verify(service).update(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product),
                Mockito.eq(stock.getIncrement()));

        JsonNode responseEntity = MAPPER.readTree(response.readEntity(InputStream.class));
        assertThat(responseEntity.asLong(), is(18L));

    }

    @Test
    public void updateFailure() throws Exception {

        String store = "store";
        String product = "product";
        String application = "application";
        Stock stock = new Stock();
        stock.setIncrement(5);

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json(JsonNodeUtils.create(Collections.singletonMap("amount", 5)).toString()));

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));

    }

    @Test
    public void updateInsufficientStockFailure() throws Exception {

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";
        Stock stock = new Stock();
        stock.setIncrement(5);

        Mockito.doThrow(new InsufficientStockException("/" + company, "/" + store, "/" + product, 100, stock.getIncrement())).when(service)
                .update(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product), Mockito.eq(stock.getIncrement()));

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json(JsonNodeUtils.create(Collections.singletonMap("increment", stock.getIncrement())).toString()));

        Mockito.verify(service).update(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product),
                Mockito.eq(stock.getIncrement()));

        assertThat(response.getStatus(), is(Status.BAD_REQUEST.getStatusCode()));
        assertThat(response.getEntity(), is(notNullValue()));

    }

    @Test
    public void get() throws Exception {

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";
        long stock = 5L;

        Mockito.when(service.get(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product))).thenReturn(stock);

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).get();

        Mockito.verify(service).get(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product));

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        JsonNode responseEntity = MAPPER.readTree(response.readEntity(InputStream.class));
        assertThat(responseEntity.get("amount").asLong(), is(5L));
        assertThat(responseEntity.has("increment"), is(false));

    }

    @Test
    public void getNoFoundStockFailure() throws Exception {

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        Mockito.when(service.get(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product)))
                .thenThrow(new NoFoundStockException("/" + store, "/" + product, new RuntimeException()));

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).get();

        Mockito.verify(service).get(Mockito.eq("/" + company), Mockito.eq("/" + store), Mockito.eq("/" + product));

        assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));

    }

}
