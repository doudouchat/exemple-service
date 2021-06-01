package com.exemple.service.api.integration.login;

import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class LoginStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private LoginTestContext context;

    @Autowired
    private LoginResource loginResource;

    @Before
    public void initKeyspace() {

        ApplicationDetail detail = new ApplicationDetail();
        detail.setKeyspace("test_keyspace");

        ResourceExecutionContext.get().setKeyspace(detail.getKeyspace());

    }

    @Given("delete username {string}")
    public void remove(String username) {

        loginResource.delete(username);

    }

    @When("create login for application {string} and version {string}")
    public void createLogin(String application, String version, JsonNode body) {

        Response response = LoginApiClient.post(body, application, version);

        context.savePost(response);

    }

    @When("delete login {string}")
    public void deleteLogin(String username) {

        Response response = LoginApiClient.delete(username, TEST_APP);

        context.saveDelete(response);

    }

    @When("get login {string} for application {string} and version {string}")
    public void getLogin(String username, String application, String version) {

        Response response = LoginApiClient.get(username, application, version);

        context.saveGet(response);

    }

    @Then("login status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @And("login {string} exists")
    public void checkExists(String username) {

        Response response = LoginApiClient.head(username, TEST_APP);

        assertThat(response.getStatusCode(), is(204));

        getLogin(username, TEST_APP, VERSION_V1);

        checkStatus(200);

    }

    @And("login {string} not exists")
    public void checkNotExists(String username) {

        Response response = LoginApiClient.head(username, TEST_APP);

        assertThat(response.getStatusCode(), is(404));

        getLogin(username, TEST_APP, VERSION_V1);

        checkStatus(404);

    }

    @And("login is")
    public void checkBody(JsonNode body) throws JsonProcessingException {

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(context.lastGet().asString());

        assertThat(expectedBody, is(body));

    }

    @And("login error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());

        assertThat(errors, is(body));

    }

}
