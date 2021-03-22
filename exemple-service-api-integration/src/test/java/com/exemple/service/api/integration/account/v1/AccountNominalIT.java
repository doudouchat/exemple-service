package com.exemple.service.api.integration.account.v1;

import static com.exemple.service.api.integration.core.InitData.APP_HEADER;
import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_HEADER;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;
import com.exemple.service.api.integration.login.v1.LoginIT;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class AccountNominalIT extends AbstractTestNGSpringContextTests {

    public static final String ACCOUNT_URL = "/ws/v1/accounts";

    protected static UUID ID = null;

    protected static Map<String, Object> ACCOUNT_BODY;

    protected static Map<String, Object> LOGIN_BODY;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void createSuccess() {

        ACCOUNT_BODY = new HashMap<>();
        ACCOUNT_BODY.put("civility", "Mr");
        ACCOUNT_BODY.put("lastname", "Dupont");
        ACCOUNT_BODY.put("firstname", "Jean");
        ACCOUNT_BODY.put("email", UUID.randomUUID().toString() + "@gmail.com");
        ACCOUNT_BODY.put("optin_mobile", true);
        ACCOUNT_BODY.put("mobile", "0610203040");
        ACCOUNT_BODY.put("birthday", "1967-06-15");

        Map<String, Object> adressesBody = new HashMap<>();

        Map<String, Object> addresse1 = new HashMap<>();
        addresse1.put("city", "Paris");
        addresse1.put("street", "rue de la paix");

        adressesBody.put("job", addresse1);

        Map<String, Object> addresse2 = new HashMap<>();
        addresse2.put("city", "Lyon");
        addresse2.put("street", "rue de la poste");

        adressesBody.put("home", addresse2);
        adressesBody.put("holiday1", null);
        adressesBody.put("holiday2", null);

        ACCOUNT_BODY.put("addresses", adressesBody);

        Map<String, Object> cgu = new HashMap<>();
        cgu.put("code", "code_1");
        cgu.put("version", "v0");

        ACCOUNT_BODY.put("cgus", Collections.singleton(cgu));

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(ACCOUNT_BODY).post(ACCOUNT_URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

        ID = UUID.fromString(response.getHeader("Location").substring(response.getHeader("Location").lastIndexOf('/') + 1));

    }

    @Test(dependsOnMethods = "createSuccess")
    public void createLogin() {

        LOGIN_BODY = new HashMap<>();
        LOGIN_BODY.put("username", ACCOUNT_BODY.get("email"));
        LOGIN_BODY.put("password", "mdp");
        LOGIN_BODY.put("id", ID);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(LOGIN_BODY).post(LoginIT.URL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "createLogin")
    public void getLoginSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(LoginIT.URL + "/{login}", ACCOUNT_BODY.get("email"));
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        assertThat(response.jsonPath().get("password"), startsWith("{bcrypt}"));
        assertThat(response.jsonPath().getString("id"), is(ID.toString()));
        assertThat(response.jsonPath().getString("username"), is(ACCOUNT_BODY.get("email")));

    }

    @Test(dependsOnMethods = "createLogin")
    public void getSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.jsonPath().getString("civility"), is(ACCOUNT_BODY.get("civility")));
        assertThat(response.jsonPath().getString("lastname"), is(ACCOUNT_BODY.get("lastname")));
        assertThat(response.jsonPath().getString("firstname"), is(ACCOUNT_BODY.get("firstname")));
        assertThat(response.jsonPath().getString("email"), is(ACCOUNT_BODY.get("email")));
        assertThat(response.jsonPath().getBoolean("optin_mobile"), is(ACCOUNT_BODY.get("optin_mobile")));
        assertThat(response.jsonPath().getString("birthday"), is(ACCOUNT_BODY.get("birthday")));
        assertThat(response.jsonPath().getString("creation_date"), is(notNullValue()));

    }

    @Test(dependsOnMethods = "getSuccess")
    public void patchSuccess() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "replace");
        patch0.put("path", "/lastname");
        patch0.put("value", "Dupond");

        patchs.add(patch0);

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "replace");
        patch1.put("path", "/firstname");
        patch1.put("value", "Roland");

        patchs.add(patch1);

        Map<String, Object> addresse = new HashMap<>();
        addresse.put("city", "New York");
        addresse.put("street", "5th avenue");

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "replace");
        patch2.put("path", "/addresses/job");
        patch2.put("value", addresse);

        patchs.add(patch2);

        Map<String, Object> patch3 = new HashMap<>();
        patch3.put("op", "remove");
        patch3.put("path", "/civility");

        patchs.add(patch3);

        Map<String, Object> cgu = new HashMap<>();
        cgu.put("code", "code_1");
        cgu.put("version", "v1");

        Map<String, Object> patch4 = new HashMap<>();
        patch4.put("op", "add");
        patch4.put("path", "/cgus/0");
        patch4.put("value", cgu);

        patchs.add(patch4);

        Map<String, Object> patch5 = new HashMap<>();
        patch5.put("op", "replace");
        patch5.put("path", "/addresses/home/city");
        patch5.put("value", "New Orleans");

        patchs.add(patch5);

        Map<String, Object> patch6 = new HashMap<>();
        patch6.put("op", "remove");
        patch6.put("path", "/addresses/job");

        patchs.add(patch6);

        Map<String, Object> patch7 = new HashMap<>();
        patch7.put("op", "add");
        patch7.put("path", "/addresses/holidays");
        patch7.put("value", addresse);

        patchs.add(patch7);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(patchs).patch(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

        Response responseGet = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(ACCOUNT_URL + "/{id}", ID);

        assertThat(responseGet.jsonPath().getString("lastname"), is(patch0.get("value")));
        assertThat(responseGet.jsonPath().getString("firstname"), is(patch1.get("value")));
        assertThat(responseGet.jsonPath().getString("email"), is(ACCOUNT_BODY.get("email")));
        assertThat(responseGet.jsonPath().getString("update_date"), is(notNullValue()));

    }

    @Test(dependsOnMethods = "patchSuccess")
    public void putSuccess() throws IOException {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(ACCOUNT_URL + "/{id}", ID);

        @SuppressWarnings("unchecked")
        Map<String, Object> model = MAPPER.convertValue(MAPPER.readTree(response.getBody().asString()), Map.class);
        model.put("firstname", "Roland");

        response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(model).put(ACCOUNT_URL + "/{id}", ID);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

        Response responseGet = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(ACCOUNT_URL + "/{id}", ID);

        assertThat(responseGet.jsonPath().getString("lastname"), is(model.get("lastname")));
        assertThat(responseGet.jsonPath().getString("firstname"), is(model.get("firstname")));
        assertThat(responseGet.jsonPath().getString("email"), is(ACCOUNT_BODY.get("email")));
        assertThat(responseGet.jsonPath().getString("update_date"), is(notNullValue()));

    }

}
