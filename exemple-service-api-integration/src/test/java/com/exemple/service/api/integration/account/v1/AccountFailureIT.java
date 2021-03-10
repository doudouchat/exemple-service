package com.exemple.service.api.integration.account.v1;

import static com.exemple.service.api.integration.account.v1.AccountNominalIT.ID;
import static com.exemple.service.api.integration.core.InitData.APP_HEADER;
import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_HEADER;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class AccountFailureIT extends AbstractTestNGSpringContextTests {

    private static final Logger LOG = LoggerFactory.getLogger(AccountFailureIT.class);

    private static final String ACCOUNT_URL = "/ws/v1/accounts";

    @DataProvider(name = "createFailure")
    private static Object[][] createFailure() {

        Map<String, Object> address = new HashMap<>();
        address.put("city", "Paris");
        address.put("street", null);

        Map<String, Object> holiday = new HashMap<>();
        holiday.put("city", "Paris");
        holiday.put("street", "rue de la paix");

        Map<String, Object> holidays = new HashMap<>();
        holidays.put("holiday1", holiday);
        holidays.put("holiday2", holiday);
        holidays.put("holiday3", holiday);

        return new Object[][] {
                // type failure
                { "lastname", 10, "/lastname", "type" },
                // field unknown
                { "unknown", "nc", "/unknown", "additionalProperties" },
                // email empty
                { "email", "", "/email", "format" },
                // date failure
                { "birthday", "2019-02-30", "/birthday", "format" },
                // boolean failure
                { "optin_mobile", 10, "/optin_mobile", "type" },
                // map field required
                { "addresses", Collections.singletonMap("job", address), "/addresses/job/street", "required" },
                // maxProperties
                { "addresses", holidays, "/addresses", "maxProperties" },
                // list failure
                { "cgus", 10, "/cgus", "type" } };
    }

    @Test(dataProvider = "createFailure")
    public void createFailure(String field, Object value, String expectedPath, String expectedCode) {

        Map<String, Object> accountBody = new HashMap<>();
        accountBody.put("email", UUID.randomUUID().toString() + "@gmail.com");
        accountBody.put("lastname", "Dupond");
        accountBody.put("firstname", "Jean");
        accountBody.put("birthday", "1970-01-01");

        accountBody.put(field, value);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(accountBody).post(ACCOUNT_URL);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertHttpClientErrorException(response, expectedPath, expectedCode);

    }

    @Test
    public void createSchemaFailure() {

        Map<String, Object> accountBody = new HashMap<>();
        accountBody.put("email", UUID.randomUUID().toString() + "@gmail.com");

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(APP_HEADER, Collections.singletonList("default"));
        headers.put(VERSION_HEADER, Collections.singletonList(UUID.randomUUID().toString()));

        Response response = JsonRestTemplate.given().body(accountBody).headers(headers).post(ACCOUNT_URL);
        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

    @DataProvider(name = "updatePatchFailures")
    private static Object[][] updatePatchFailures() {

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "replace");
        patch0.put("path", "/lastname");
        patch0.put("value", 10);

        Map<String, Object> patch1 = new HashMap<>();
        patch1.put("op", "add");
        patch1.put("path", "/nc");
        patch1.put("value", "nc");

        Map<String, Object> cgu = new HashMap<>();
        cgu.put("code", "code_1");
        cgu.put("version", "v1");

        Map<String, Object> patch2 = new HashMap<>();
        patch2.put("op", "replace");
        patch2.put("path", "/cgus/0");
        patch2.put("value", cgu);

        cgu = new HashMap<>();
        cgu.put("code", "code_1");
        cgu.put("version", "v2");

        Map<String, Object> patch3 = new HashMap<>();
        patch3.put("op", "add");
        patch3.put("path", "/cgus/0");
        patch3.put("value", cgu);

        Map<String, Object> patch4 = new HashMap<>();
        patch4.put("op", "replace");
        patch4.put("path", "/email");
        patch4.put("value", "jean.dupond@gmail.com");

        Map<String, Object> patch5 = new HashMap<>();
        patch5.put("op", "remove");
        patch5.put("path", "/mobile");

        Map<String, Object> holidays = new HashMap<>();

        Map<String, Object> holiday = new HashMap<>();
        holiday.put("city", "Paris");
        holiday.put("street", "rue de la paix");

        holidays.put("holiday1", holiday);
        holidays.put("holiday2", holiday);
        holidays.put("holiday3", holiday);

        Map<String, Object> patch6 = new HashMap<>();
        patch6.put("op", "add");
        patch6.put("path", "/addresses");
        patch6.put("value", holidays);

        holidays = new HashMap<>();
        holidays.put("holiday1", holiday);
        Map<String, Object> patch7 = new HashMap<>();
        patch7.put("op", "add");
        patch7.put("path", "/addresses");
        patch7.put("value", holidays);

        return new Object[][] {
                // type failure
                { patch0, "/lastname", "type" },
                // field unknown
                { patch1, "/nc", "additionalProperties" },
                // uniqueItems
                { patch2, "/cgus", "uniqueItems" },
                // maxitems
                { patch3, "/cgus", "maxItems" },
                // FIXME login
                // { patch4, "/email", "login" },
                // required mobile
                { patch5, "/mobile", "required" },
                // maxProperties
                { patch6, "/addresses", "maxProperties" },
                // FIXME maxProperties
                // { patch7, "/addresses", "maxProperties" },
                //
        };
    }

    @Test(dataProvider = "updatePatchFailures", dependsOnMethods = "com.exemple.service.api.integration.account.v1.AccountNominalIT.updateSuccess")
    public void updatePatchFailures(Map<String, Object> patch, String expectedPath, String expectedCode) {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(Collections.singletonList(patch)).patch(ACCOUNT_URL + "/{id}", ID);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertHttpClientErrorException(response, expectedPath, expectedCode);

    }

    private static void assertHttpClientErrorException(Response response, String expectedPath, String expectedCode) {

        assertThat(response.jsonPath().getList("code").get(0), is(expectedCode));
        assertThat(response.jsonPath().getList("path").get(0), is(expectedPath));
    }

    @Test(dependsOnMethods = "com.exemple.service.api.integration.account.v1.AccountNominalIT.updateSuccess")
    public void getSchemaFailure() {

        Map<String, List<String>> headers = new HashMap<>();
        headers.put(APP_HEADER, Collections.singletonList("default"));
        headers.put(VERSION_HEADER, Collections.singletonList(UUID.randomUUID().toString()));

        Response response = JsonRestTemplate.given().headers(headers).get(ACCOUNT_URL + "/{id}", ID);
        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN.value()));

    }

    @Test
    public void getNotFound() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(ACCOUNT_URL + "/{id}", UUID.randomUUID());
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }

    @DataProvider(name = "updateFailure")
    private static Object[][] updateFailure() {

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "replace");
        patch0.put("path", "/lastname");

        return new Object[][] {
                // patch empty
                { Collections.EMPTY_LIST },
                // patch without value
                { Collections.singletonList(patch0) }
                //
        };
    }

    @Test(dataProvider = "updateFailure", dependsOnMethods = "com.exemple.service.api.integration.account.v1.AccountNominalIT.updateSuccess")
    public void updateFailure(List<Map<String, Object>> patchs) {

        LOG.debug("{}", JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .get(ACCOUNT_URL + "/{id}", ID));

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(patchs).patch(ACCOUNT_URL + "/{id}", ID);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));

    }

    @Test
    public void updateNotFound() {

        List<Map<String, Object>> patchs = new ArrayList<>();

        Map<String, Object> patch0 = new HashMap<>();
        patch0.put("op", "replace");
        patch0.put("path", "/lastname");
        patch0.put("value", "Dupond");

        patchs.add(patch0);

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, TEST_APP).header(VERSION_HEADER, VERSION_V1)

                .body(patchs).patch(ACCOUNT_URL + "/{id}", UUID.randomUUID());
        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }
}
