package com.exemple.service.api.integration.subscription;

import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.customer.subscription.SubscriptionResource;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class SubscriptionStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SubscriptionTestContext context;

    @Autowired
    private SubscriptionResource subscriptionResource;

    @Before
    public void initKeyspace() {

        ResourceExecutionContext.get().setKeyspace("test_keyspace");

    }

    @Given("delete subscription {string}")
    public void remove(String email) {

        subscriptionResource.delete(email);

    }

    @When("create subscription {string} for application {string} and version {string}")
    public void createSubscription(String email, String application, String version) {

        Response response = SubscriptionApiClient.put(email, Collections.emptyMap(), application, version);

        context.savePut(response);

    }

    @When("get subscription {string} for application {string} and version {string}")
    public void getSubscription(String email, String application, String version) {

        Response response = SubscriptionApiClient.get(email, application, version);

        context.saveGet(response);

    }

    @Then("subscription status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode()).isEqualTo(status);

    }

    @And("subscription {string} is")
    public void checkBody(String email, JsonNode body) throws JsonProcessingException {

        getSubscription(email, TEST_APP, VERSION_V1);

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(context.lastGet().asString());
        expectedBody.remove("subscription_date");

        assertThat(expectedBody).isEqualTo(body);

    }

    @And("subscription contains {string}")
    public void checkProperty(String property) {

        assertThat(context.lastGet().jsonPath().getString(property)).isNotNull();

    }

    @And("subscription error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastPut().asString());
        Streams.stream(errors.elements()).map(ObjectNode.class::cast).forEach((ObjectNode error) -> error.remove("message"));

        assertThat(errors).isEqualTo(body);

    }

}
