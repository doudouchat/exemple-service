package com.exemple.service.api.integration.stock;

import static com.exemple.service.api.integration.core.InitData.BACK_APP;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class StockStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private StockTestContext context;

    private UUID salt;

    @Before
    public void initKeyspace() {

        salt = UUID.randomUUID();

    }

    @When("increase of {long} for product {string} from store {string}")
    public void increment(long increment, String product, String store) {

        Map<String, Object> body = Collections.singletonMap("increment", increment);

        Response response = StockApiClient.post(store + "#" + salt, product, body, BACK_APP);

        context.savePost(response);

    }

    @When("get stock of product {string} from store {string}")
    public void getLogin(String product, String store) {

        Response response = StockApiClient.get(store + "#" + salt, product, BACK_APP);

        context.saveGet(response);

    }

    @Then("stock status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode()).isEqualTo(status);

    }

    @And("stock of product {string} from store {string} is {long}")
    public void check(String product, String store, long amount) throws JsonProcessingException {

        Response response = StockApiClient.get(store + "#" + salt, product, BACK_APP);

        assertThat(response.jsonPath().getLong("amount")).isEqualTo(amount);

    }

    @And("stock of product {string} from store {string} is {long}, is insufficient for {long}")
    public void checkError(String product, String store, long stock, long quantity) throws JsonProcessingException {

        assertThat(context.lastResponse().getBody().asString())
                .isEqualTo("Stock /test_company/" + store + "#" + salt + "/" + product + ":" + stock + " is insufficient for quantity " + quantity);

    }

    @And("stock error is expect {string}")
    public void checkError(String expect, JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        Streams.stream(errors.elements()).map(ObjectNode.class::cast).forEach((ObjectNode error) -> error.remove(expect));

        assertThat(errors).isEqualTo(body);

    }

}
