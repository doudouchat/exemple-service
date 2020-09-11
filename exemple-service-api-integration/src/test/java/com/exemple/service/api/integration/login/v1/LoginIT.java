package com.exemple.service.api.integration.login.v1;

import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class LoginIT extends AbstractTestNGSpringContextTests {

    public static final String URL = "/ws/v1/logins";

    private static final String LOGIN = UUID.randomUUID() + "@gmail.com";

    private static final UUID ID = UUID.randomUUID();

    @Test
    public void create() {

        Map<String, Object> body = new HashMap<>();
        body.put("username", LOGIN);
        body.put("password", "mdp");
        body.put("id", ID);

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

    @Test(dependsOnMethods = "create")
    public void get() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .get(URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(notNullValue()));
        assertThat(response.jsonPath().get("password"), startsWith("{bcrypt}"));
        assertThat(BCrypt.checkpw("mdp", response.jsonPath().getString("password").substring("{bcrypt}".length())), is(true));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(LOGIN));

    }

    @DataProvider(name = "updateSuccess")
    private static Object[][] updateSuccess() {

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "add");
        patch0.put("path", "/password");
        patch0.put("value", "new_mdp");

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "add");
        patch1.put("path", "/disable");
        patch1.put("value", true);

        return new Object[][] {
                // modify password
                { patch0 },
                // disable is true
                { patch1 } };
    }

    @Test(dataProvider = "updateSuccess", dependsOnMethods = { "exist", "get" })
    public void update(Map<String, Object> patch) {

        List<Map<String, Object>> patchs = new ArrayList<>();
        patchs.add(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test(dependsOnMethods = "update")
    public void getLoginSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .get(LoginIT.URL + "/{login}", LOGIN);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), is(notNullValue()));
        assertThat(response.jsonPath().get("password"), startsWith("{bcrypt}"));
        assertThat(BCrypt.checkpw("new_mdp", response.jsonPath().getString("password").substring("{bcrypt}".length())), is(true));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(LOGIN));
        assertThat(response.jsonPath().getString("disable"), is("true"));

    }

    @DataProvider(name = "updateFailure")
    private static Object[][] updateFailure() {

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "replace");
        patch0.put("path", "/id");
        patch0.put("value", UUID.randomUUID());

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "replace");
        patch1.put("path", "/username");
        patch1.put("value", "jean.dupond@gmail.com");

        return new Object[][] {
                // id is readOnly
                { patch0, "/id", "readOnly" },
                // username is unique
                { patch1, "/username", "login" } };
    }

    @Test(dataProvider = "updateFailure", dependsOnMethods = "create")
    public void updateFailure(Map<String, Object> patch, String expectedPath, String expectedCode) {

        List<Map<String, Object>> patchs = Collections.singletonList(patch);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(patchs).patch(LoginIT.URL + "/{login}", LOGIN);

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code").get(0), is(expectedCode));
        assertThat(response.jsonPath().getList("path").get(0), is(expectedPath));

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
