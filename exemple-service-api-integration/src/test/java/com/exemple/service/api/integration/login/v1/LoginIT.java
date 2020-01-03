package com.exemple.service.api.integration.login.v1;

import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public class LoginIT {

    public static final String URL = "/ws/v1/logins";

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    @Test
    public void create() {

        Map<String, Object> body = new HashMap<>();
        body.put("login", LOGIN);
        body.put("password", "mdp");
        body.put("id", UUID.randomUUID());

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .body(body).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "create")
    public void exist() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE)

                .head(URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "exist")
    public void update() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "add");
        patch.put("path", "/password");
        patch.put("value", "new_mdp");

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "exist")
    public void updateFailure() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch = new HashMap<>();
        patch.put("op", "replace");
        patch.put("path", "/id");
        patch.put("value", UUID.randomUUID());

        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));

    }

    @Test(dependsOnMethods = { "update", "updateFailure" })
    public void delete() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE)

                .delete(URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "delete")
    public void notFound() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE)

                .head(URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
