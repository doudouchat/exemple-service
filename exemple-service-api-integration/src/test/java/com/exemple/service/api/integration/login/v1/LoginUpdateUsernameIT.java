package com.exemple.service.api.integration.login.v1;

import static com.exemple.service.api.integration.core.InitData.APP_HEADER;
import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_HEADER;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public class LoginUpdateUsernameIT {

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

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(body).post(LoginIT.URL);

        assertThat(create.getStatusCode(), is(HttpStatus.CREATED.value()));

        // update

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "add");
        patch1.put("path", "/username");
        patch1.put("value", NEW_LOGIN);

        patchs.add(patch1);

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "add");
        patch2.put("path", "/disable");
        patch2.put("value", true);

        patchs.add(patch2);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(LoginIT.URL + "/{login}", NEW_LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(notNullValue()));
        assertThat(response.jsonPath().get("password"), startsWith("{bcrypt}"));
        assertThat(BCrypt.checkpw("mdp", response.jsonPath().getString("password").substring("{bcrypt}".length())), is(true));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(NEW_LOGIN));
        assertThat(response.jsonPath().getString("disable"), is("true"));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(LoginIT.URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
