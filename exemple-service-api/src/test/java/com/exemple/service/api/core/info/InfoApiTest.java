package com.exemple.service.api.core.info;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.Viewable;
import org.glassfish.jersey.server.mvc.spi.TemplateProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.exemple.service.api.core.ApiTestConfiguration;
import com.exemple.service.api.core.JerseySpringSupport;
import com.exemple.service.api.core.authorization.AuthorizationTestConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@SpringBootTest(classes = { ApiTestConfiguration.class, AuthorizationTestConfiguration.class })
@ActiveProfiles({ "test", "AuthorizationMock" })
class InfoApiTest extends JerseySpringSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected ResourceConfig configure() {
        return new InfoConfiguration().register(TemplateTestProcessor.class, 1);
    }

    private static final String URL = "/";

    @Test
    void template() throws IOException {

        // When perform get

        Response response = target(URL).request().get();

        // Then check status

        assertThat(response.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // And check body

        assertThat(response.readEntity(String.class)).isEqualTo(MAPPER.readTree(
                """
                {"version":"nc","buildTime":"nc"}
                """).toString());

    }

    private static class TemplateTestProcessor implements TemplateProcessor<String> {

        @Override
        public String resolve(String name, MediaType mediaType) {
            return name;
        }

        @Override
        public void writeTo(String templateReference, Viewable viewable, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                OutputStream out) throws IOException {
            PrintStream ps = new PrintStream(out);
            ps.print(MAPPER.writeValueAsString(viewable.getModel()));

        }

    }

}
