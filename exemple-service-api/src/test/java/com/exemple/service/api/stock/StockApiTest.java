package com.exemple.service.api.stock;

import static com.exemple.service.api.common.model.ApplicationBeanParam.APP_HEADER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.stock.StockService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@SpringBootTest(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles({ "test", "AuthorizationMock" })
class StockApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Autowired
    private StockService service;

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @BeforeEach
    void before() {

        Mockito.reset(service, applicationDetailService);

    }

    public static final String URL = "/v1/stocks";

    @Nested
    class increment {

        @Test
        void success() throws InsufficientStockException {

            // Given stock

            String store = "store";
            String product = "product";
            String company = "company1";
            String application = "application";

            // And mock service

            Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

            Mockito.doNothing().when(service).increment(company, store, product, 5);

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform post

            Response response = target(URL + "/" + store + "/" + product + "/_increment")
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .post(Entity.text("5"));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NO_CONTENT.getStatusCode());

        }

        static Stream<Arguments> validationFailure() throws IOException {

            return Stream.of(
                    Arguments.of("", MAPPER.readTree(
                            """
                            {"increment":"La valeur doit être un entier acceptable."}
                            """)),
                    Arguments.of("qsqsqs", MAPPER.readTree(
                            """
                            {"increment":"La valeur doit être un entier acceptable."}
                            """)));
        }

        @ParameterizedTest
        @MethodSource
        void validationFailure(String body, JsonNode expectedMessage) {

            // Given stock

            String store = "store";
            String product = "product";
            String application = "application";

            // And mock service

            Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform post

            Response response = target(URL + "/" + store + "/" + product + "/_increment")
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .post(Entity.text(body));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

            // And check body

            assertThat(response.readEntity(JsonNode.class)).isEqualTo(expectedMessage);

        }

        @Test
        void insufficientStock() throws InsufficientStockException {

            // Given stock

            String store = "store";
            String product = "product";
            String company = "company1";
            String application = "application";

            // And mock service

            Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

            Mockito.doThrow(new InsufficientStockException(store, product, 100, 5)).when(service).increment(company, store, product, 5);

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            Response response = target(URL + "/" + store + "/" + product + "/_increment")
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .post(Entity.text(5));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

            // And check body

            assertThat(response.readEntity(String.class)).isEqualTo(new InsufficientStockException(store, product, 100, 5).getMessage());

        }

        @Test
        void isForbidden() throws InsufficientStockException {

            // Given stock

            String store = "store";
            String product = "product";
            String application = "application";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform put

            Response response = target(URL + "/" + store + "/" + product + "/_increment")
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .post(Entity.text(5));

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // verify service

            Mockito.verify(service, never()).increment("application", "store", "product", 5);

        }

    }

    @Nested
    class get {

        @Test
        void success() throws IOException {

            // Given stock

            String store = "store";
            String product = "product";
            String company = "company1";
            String application = "application";

            // And mock service

            Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

            Mockito.when(service.get(company, store, product)).thenReturn(Optional.of(5L));

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform service

            Response response = target(URL + "/" + store + "/" + product)
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

            // and check body

            assertThat(response.readEntity(JsonNode.class)).isEqualTo(MAPPER.readTree(
                    """
                    {"amount":5, "store": "%s", "product": "%s"}
                    """.formatted(store, product)));

        }

        @Test
        void notFound() {

            // Given stock

            String store = "store";
            String product = "product";
            String company = "company1";
            String application = "application";

            // And mock service

            Mockito.when(applicationDetailService.get(application)).thenReturn(Optional.of(ApplicationDetail.builder().company("company1").build()));

            Mockito.when(service.get("/" + company, "/" + store, "/" + product)).thenReturn(Optional.empty());

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:read" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform service

            Response response = target(URL + "/" + store + "/" + product)
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

        }

        @Test
        void isForbidden() {

            // Given stock

            String store = "store";
            String product = "product";
            String application = "application";

            // and token

            var payload = new JWTClaimsSet.Builder()
                    .claim("scope", new String[] { "stock:update" })
                    .build();

            var token = new PlainJWT(payload).serialize();

            // When perform service

            Response response = target(URL + "/" + store + "/" + product)
                    .request(MediaType.APPLICATION_JSON).header(APP_HEADER, application).header("Authorization", token)
                    .get();

            // Then check status

            assertThat(response.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());

            // verify service

            Mockito.verify(service, never()).get(application, store, product);

        }

    }

}
