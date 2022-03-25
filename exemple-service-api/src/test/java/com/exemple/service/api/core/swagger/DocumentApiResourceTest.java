package com.exemple.service.api.core.swagger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.JerseySpringSupportSecure;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;
import com.exemple.service.schema.description.SchemaDescription;
import com.fasterxml.jackson.databind.JsonNode;

@TestMethodOrder(OrderAnnotation.class)
public class DocumentApiResourceTest extends JerseySpringSupport {

    public static final String URL = "/test/openapi.json";

    private static final String ACCOUNT_V1_URL = "/ws/v1/schemas/account/test/v1/user";

    private static final String ACCOUNT_V2_URL = "/ws/v1/schemas/account/test/v2/admin";

    private static final String PATCH_URL = "/ws/v1/schemas/patch";

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private SchemaDescription schemaDescription;

    @Autowired
    private JsonNode schema;

    @Autowired
    private JsonNode swagger;

    @BeforeEach
    private void before() {

        Mockito.reset(schemaResource);

    }

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration();
    }

    @Test
    @Order(1)
    public void swagger() {

        // Given service mock

        Mockito.when(schemaResource.allVersions(Mockito.anyString())).thenReturn(
                Collections.singletonMap("account", Arrays.asList(SchemaVersionProfileEntity.builder().version("v1").profile("user").build(),
                        SchemaVersionProfileEntity.builder().version("v2").profile("admin").build())));

        // When perform get

        Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(swagger));

    }

    private Stream<Arguments> schemas() {

        return Stream.of(
                Arguments.of(ACCOUNT_V1_URL),
                Arguments.of(ACCOUNT_V2_URL));
    }

    @ParameterizedTest
    @MethodSource
    @Order(2)
    public void schemas(String url) throws Exception {

        // Given mock service

        Mockito.when(schemaDescription.get(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(schema);

        // When perform get

        Response response = target(url.replaceFirst("ws/", "")).request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(schema));

    }

    @Test
    @Order(2)
    public void patch() {

        // Given mock service

        Mockito.when(schemaDescription.getPatch()).thenReturn(schema);

        // When perform get

        Response response = target(PATCH_URL.replaceFirst("ws/", "")).request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

        // And check body

        assertThat(response.readEntity(JsonNode.class), is(schema));

    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class Security extends JerseySpringSupportSecure {

        @Override
        protected ResourceConfig configure() {
            return new FeatureConfiguration();
        }

        @Autowired
        @Qualifier("swagger_security")
        private JsonNode swaggerSecurity;

        @Test
        public void swagger() {

            // When perform get

            Response response = target(URL).request(MediaType.APPLICATION_JSON).get();

            // Then check status

            assertThat(response.getStatus(), is(Status.OK.getStatusCode()));

            // And check body

            JsonNode swagger = response.readEntity(JsonNode.class);
            assertThat(swagger.at("/components/securitySchemes"), is(swaggerSecurity));

        }

    }

}
