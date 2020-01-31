package com.exemple.service.api.integration.actuate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public class HeathIT {

    private static final String URL = "/actuator/health";

    @Test
    public void health() {

        Response response = JsonRestTemplate.given().get(URL);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("status"), is("UP"));

    }
}
