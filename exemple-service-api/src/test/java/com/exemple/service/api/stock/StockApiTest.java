package com.exemple.service.api.stock;

import static com.exemple.service.api.common.model.ApplicationBeanParam.APP_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;

class StockApiTest extends JerseySpringSupport {

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private StockService service;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @BeforeEach
    private void before() {

        Mockito.reset(service, applicationDetailService);

    }

    public static final String URL = "/v1/stocks";

    @Test
    void update() throws InsufficientStockException {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

        Mockito.when(service.update("/" + company, "/" + store, "/" + product, 5)).thenReturn(18L);

        // When perform post

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json("{\"increment\":5}"));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check body

        assertThat(response.readEntity(Long.class)).isEqualTo(18L);

    }

    @Test
    void updateValidationFailure() {

        // Given stock

        String store = "store";
        String product = "product";
        String application = "application";

        // And mock service

        Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

        // When perform post

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json("{}"));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("{\"increment\":\"La valeur doit être renseignée.\"}");

    }

    @Test
    void updateInsufficientStockFailure() throws InsufficientStockException {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

        Mockito.doThrow(new InsufficientStockException("/" + company, "/" + store, "/" + product, 100, 5)).when(service)
                .update("/" + company, "/" + store, "/" + product, 5);

        // When perform put

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application)
                .post(Entity.json("{\"increment\":5}"));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(String.class))
                .isEqualTo(new InsufficientStockException("/" + company, "/" + store, "/" + product, 100, 5).getMessage());

    }

    @Test
    void get() {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

        Mockito.when(service.get("/" + company, "/" + store, "/" + product)).thenReturn(Optional.of(5L));

        // When perform service

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // and check body

        JsonNode responseEntity = response.readEntity(JsonNode.class);
        assertAll(
                () -> assertThat(responseEntity.get("amount").asLong()).isEqualTo(5L),
                () -> assertThat(responseEntity.has("increment")).isFalse());

    }

    @Test
    void getNotFoundStockFailure() {

        // Given stock

        String store = "store";
        String product = "product";
        String company = "company1";
        String application = "application";

        // And mock service

        Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

        Mockito.when(service.get("/" + company, "/" + store, "/" + product)).thenReturn(Optional.empty());

        // When perform service

        Response response = target(URL + "/" + store + "/" + product).request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    }

}
