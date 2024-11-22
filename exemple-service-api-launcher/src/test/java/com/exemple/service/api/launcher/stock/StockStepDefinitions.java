package com.exemple.service.api.launcher.stock;

import static com.exemple.service.api.launcher.core.InitData.BACK_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.launcher.authorization.AuthorizationTestContext;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class StockStepDefinitions {

    @Autowired
    private StockTestContext context;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    private UUID salt;

    @Before
    public void initKeyspace() {

        salt = UUID.randomUUID();

    }

    @When("increase of {long} for product {string} from store {string}")
    public void increment(long increment, String product, String store) {

        Map<String, Object> body = Collections.singletonMap("increment", increment);

        Response response = StockApiClient.post(store + "#" + salt, product, body, BACK_APP, authorizationContext.lastAccessToken());

        context.savePost(response);

    }

    @When("get stock of product {string} from store {string}")
    public void getLogin(String product, String store) {

        Response response = StockApiClient.get(store + "#" + salt, product, BACK_APP, authorizationContext.lastAccessToken());

        context.saveGet(response);

    }

    @Then("stock of product {string} from store {string} is {long}")
    public void check(String product, String store, long amount) {

        Response response = StockApiClient.get(store + "#" + salt, product, BACK_APP, authorizationContext.lastAccessToken());

        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).as("stock %s %s not found", product, store).isEqualTo(200),
                () -> assertThat(response.jsonPath().getLong("amount")).as("stock %s %s has bad ammount", product, store).isEqualTo(amount));

    }

    @Then("stock of product {string} from store {string} is unknown")
    public void checkUnknown(String product, String store) {

        assertThat(context.lastResponse().getStatusCode()).as("stock %s %s exists", product, store).isEqualTo(404);

    }

    @Then("stock of product {string} from store {string} is {long}, is insufficient for {long}")
    public void checkError(String product, String store, long stock, long quantity) {

        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).as("stock %s %s is correct", product, store).isEqualTo(400),
                () -> assertThat(context.lastResponse().getBody().asString())
                        .isEqualTo("Stock /test_company/" + store + "#" + salt + "/" + product + ":" + stock + " is insufficient for quantity "
                                + quantity));

    }

}
