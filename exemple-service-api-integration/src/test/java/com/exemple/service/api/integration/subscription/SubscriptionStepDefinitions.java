package com.exemple.service.api.integration.subscription;

import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.SubscriptionResource;
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

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test_keyspace");

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

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

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @And("subscription {string} exists")
    public void checkExists(String email) {

        getSubscription(email, TEST_APP, VERSION_V1);

        checkStatus(200);

    }

    @And("subscription is")
    public void checkBody(JsonNode body) throws JsonProcessingException {

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(context.lastGet().asString());
        expectedBody.remove("subscription_date");

        assertThat(expectedBody, is(body));

    }

    @And("subscription contains {string}")
    public void checkProperty(String property) {

        assertThat(context.lastGet().jsonPath().getString(property), is(notNullValue()));

    }

    @And("subscription error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastPut().asString());
        Streams.stream(errors.elements()).map(ObjectNode.class::cast).forEach((ObjectNode error) -> error.remove("message"));

        assertThat(errors, is(body));

    }

}
