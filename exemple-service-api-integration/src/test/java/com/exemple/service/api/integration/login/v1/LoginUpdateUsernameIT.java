package com.exemple.service.api.integration.login.v1;

import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public class LoginUpdateUsernameIT {

    public static final String URL = "/ws/v1/logins";

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final String NEW_LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    @Test
    public void update() {

        // create login

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN);
        body.put("password", "mdp");
        body.put("id", ID);

        Response create = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .body(body).post(LoginUpdateUsernameIT.URL);

        assertThat(create.getStatusCode(), is(HttpStatus.CREATED.value()));

        // update

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "add");
        patch1.put("path", "/password");
        patch1.put("value", "new_mdp");

        patchs.add(patch1);

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "add");
        patch2.put("path", "/username");
        patch2.put("value", NEW_LOGIN);

        patchs.add(patch2);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(patchs).patch(LoginUpdateUsernameIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "update")
    public void getNewLogin() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .get(URL + "/{login}", NEW_LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(nullValue()));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(NEW_LOGIN));

    }

    @Test(dependsOnMethods = "update")
    public void getOldLogin() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .get(URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
