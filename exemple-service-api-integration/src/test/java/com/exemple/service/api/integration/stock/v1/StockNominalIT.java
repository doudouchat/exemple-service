package com.exemple.service.api.integration.stock.v1;

import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class StockNominalIT extends AbstractTestNGSpringContextTests {

    public static final String APP_HEADER_VALUE = "back";

    private static final String STOCK_URL = "/ws/v1/stocks/{store}/{product}";

    private final String product = "product#" + UUID.randomUUID();

    private final String store = "store#" + UUID.randomUUID();

    @DataProvider(name = "updateSuccess")
    private static Object[][] updateSuccess() {

        return new Object[][] {

                { 5 },

                { 8 }

        };
    }

    @Test(dataProvider = "updateSuccess")
    public void updateSuccess(int increment) {

        Response response = JsonRestTemplate.given().body(Collections.singletonMap("increment", increment))

                .header(APP_HEADER, APP_HEADER_VALUE)

                .post(STOCK_URL, store, product);
        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    @Test(dependsOnMethods = "updateSuccess")
    public void getSuccess() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE)

                .get(STOCK_URL, store, product);

        assertThat(response.jsonPath().getInt("amount"), is(13));

    }

    @Test(dependsOnMethods = "getSuccess")
    public void getFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE)

                .get(STOCK_URL, store, "product#" + UUID.randomUUID());

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));

    }

    @Test(dependsOnMethods = "getSuccess")
    public void updateFailure() {

        Response response = JsonRestTemplate.given().body(Collections.singletonMap("increment", -100))

                .header(APP_HEADER, APP_HEADER_VALUE)

                .post(STOCK_URL, store, product);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.getBody().asString(), is("Stock /test_company/" + store + "/" + product + ":13 is insufficient for qunatity -100"));

    }

}
