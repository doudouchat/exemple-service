package com.exemple.service.api.integration.swagger;

import static com.exemple.service.api.integration.core.IntegrationTestConfiguration.TEST_APP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class SwaggerIT extends AbstractTestNGSpringContextTests {

    @Test
    public void swagger() {

        Response response = JsonRestTemplate.given().get("/ws/" + TEST_APP + "/openapi.json");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

    @Test
    public void schema() {

        Response response = JsonRestTemplate.given().get("/ws/v1/schemas/account/" + TEST_APP + "/v1/user");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

    @Test
    public void patch() {

        Response response = JsonRestTemplate.given().get("/ws/v1/schemas/patch");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

}
