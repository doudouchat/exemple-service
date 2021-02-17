package com.exemple.service.api.integration.login.v1;

import static com.exemple.service.api.integration.core.InitData.APP_HEADER;
import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_HEADER;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class LoginMultipleIT {

    private static final String LOGIN_1 = UUID.randomUUID() + "@gmail.com";

    private static final String LOGIN_2 = UUID.randomUUID() + "@gmail.com";

    private static final String LOGIN_3 = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    @Test
    public void createMultiple() {

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN_1);
        body.put("password", "mdp");
        body.put("id", ID);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(body).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        body.put("username", LOGIN_2);

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(body).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        body.put("username", LOGIN_3);
        body.put("id", UUID.randomUUID());

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(body).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "createMultiple")
    public void getSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(LoginIT.URL + "/{login}", LOGIN_2);

        assertThat(response.jsonPath().getString("password"), startsWith("{bcrypt}"));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(LOGIN_2));

    }

    @Test(dependsOnMethods = "createMultiple")
    public void getById() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(LoginIT.URL + "/id/{id}", ID);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().getList("password"), everyItem(startsWith("{bcrypt}")));
        assertThat(response.jsonPath().getList("id"), everyItem(is(ID.toString())));
        assertThat(response.jsonPath().getList("username"), containsInAnyOrder(equalTo(LOGIN_1), equalTo(LOGIN_2)));

    }
    
    @Test
    public void notFound() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(LoginIT.URL + "/id/{id}", UUID.randomUUID());

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
