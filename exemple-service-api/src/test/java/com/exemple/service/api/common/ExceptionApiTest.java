package com.exemple.service.api.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.UUID;

import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.exemple.service.api.core.check.AppAndVersionCheck;
import com.exemple.service.api.core.feature.FeatureConfiguration;
import com.exemple.service.customer.common.validator.NotEmpty;
import com.exemple.service.schema.validation.annotation.Patch;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.Builder;
import lombok.Getter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;
import tools.jackson.databind.node.ArrayNode;

@SpringBootTest(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles({ "test", "AuthorizationMock" })
class ExceptionApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static Action action = Mockito.mock(Action.class);

    private static final String URL = "/v1/test";

    @Override
    protected ResourceConfig configure() {
        return new FeatureConfiguration().register(TestApi.class);
    }

    @Test
    void notFound() {

        // When perform get

        Response response = target("/v1/notfound").request(MediaType.APPLICATION_JSON).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

    }

    @Test
    void notAcceptable() {

        // When perform get

        Response response = target("/v1/test").request(MediaType.TEXT_HTML).get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.NOT_ACCEPTABLE.getStatusCode());

    }

    @Test
    void JsonException() {

        // When perform post

        Response response = target("/v1/test")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json("toto"));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(String.class)).startsWith("Unrecognized token 'toto'");
    }

    @Test
    void JsonEmptyException() {

        // When perform patch

        Response response = target(URL + "/" + UUID.randomUUID()).property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON)
                .method("PATCH", Entity.json(Collections.EMPTY_LIST));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(JsonNode.class))
                .isEqualTo(MAPPER.readTree(
                        """
                        {"update":"Le json doit être renseigné."}
                        """));
    }

    @Test
    void unrecognizedPropertyException() {

        // When perform post

        Response response = target(URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.json(
                        """
                        {"lastname":"jean"}
                        """));

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo("One or more fields are unrecognized");

    }

    @Test
    void internalServerError() {

        // Given mock service

        Mockito.doThrow(new RuntimeException()).when(action).execute();

        // When perform get

        Response response = target(URL).request(MediaType.APPLICATION_JSON)
                .header(SchemaBeanParam.APP_HEADER, "test").header(SchemaBeanParam.VERSION_HEADER, "v1")
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.INTERNAL_SERVER_ERROR.getStatusCode());

    }

    @Test
    void appAndVersionAreMissing() {

        // When perform get

        Response response = target(URL).request(MediaType.APPLICATION_JSON)
                .get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());

        // And check body

        assertThat(response.readEntity(JsonNode.class))
                .isEqualTo(MAPPER.readTree(
                        """
                        {"app":"La valeur doit être renseignée.","version":"La valeur doit être renseignée."}
                        """));

    }

    @Path("/v1/test")
    @Hidden
    public static class TestApi {

        @GET
        @Produces(MediaType.APPLICATION_JSON)
        @AppAndVersionCheck
        public Response get() {

            action.execute();

            return Response.ok().build();

        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        public Response post(Test resource) {

            return Response.ok().build();

        }

        @PATCH
        @Path("/{id}")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response update(@PathParam("id") String id, @NotEmpty @Patch ArrayNode patch) {

            return Response.ok().build();

        }

        @Getter
        @Builder
        // TODO move to @Jacksonized
        @JsonDeserialize(builder = Test.TestBuilder.class)
        private static class Test {

            private final String name;

            @JsonPOJOBuilder(withPrefix = "")
            public static class TestBuilder {
            }

        }

    }

    private static class Action {

        public void execute() {
            // NOP
        }
    }

}
