package com.exemple.service.api.integration.account;

import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class AccountStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AccountTestContext context;

    @When("create account for application {string} and version {string}")
    public void createAccount(String application, String version, JsonNode body) {

        Response response = AccountApiClient.post(body, application, version);

        context.savePost(response);

        if (StringUtils.isNotEmpty(response.getHeader("Location"))) {
            UUID id = UUID.fromString(response.getHeader("Location").substring(response.getHeader("Location").lastIndexOf('/') + 1));
            context.saveId(id);
        }

    }

    @When("patch account for application {string} and version {string}")
    public void patchAccount(String application, String version, JsonNode body) {

        Response response = AccountApiClient.patch(context.lastId(), body, application, version);

        context.savePatch(response);

    }

    @When("put account for application {string} and version {string}")
    public void putAccount(String application, String version, JsonNode body) {

        Response responseGet = AccountApiClient.get(context.lastId(), application, version);

        ((ObjectNode) body).put("id", responseGet.jsonPath().getString("id"));
        ((ObjectNode) body).put("creation_date", responseGet.jsonPath().getString("creation_date"));

        Response response = AccountApiClient.put(context.lastId(), body, application, version);

        context.savePut(response);

    }

    @When("create account with {string} and value {int}")
    public void createAccount(String property, int value) throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");
        Map<String, Object> body = JsonPath.parse(resource.getInputStream()).set(property, value).json();

        createAccount(TEST_APP, VERSION_V1, MAPPER.convertValue(body, JsonNode.class));

    }

    @When("create account with {string} and value {string}")
    public void createAccount(String property, String value) throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");

        DocumentContext account = JsonPath.parse(resource.getInputStream());
        Map<String, Object> body = account.put("$", property, value).json();

        createAccount(TEST_APP, VERSION_V1, MAPPER.convertValue(body, JsonNode.class));

    }

    @When("create account with {string}")
    public void createAccount(String property, JsonNode value) throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");

        DocumentContext account = JsonPath.parse(resource.getInputStream());
        Map<String, Object> body = account.put("$", property, value).json();

        createAccount(TEST_APP, VERSION_V1, MAPPER.convertValue(body, JsonNode.class));

    }

    @Given("create account")
    public void createAccount() throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");

        Map<String, Object> body = JsonPath.parse(resource.getInputStream()).json();

        createAccount(TEST_APP, VERSION_V1, MAPPER.convertValue(body, JsonNode.class));

    }

    @When("get account {id} for application {string} and version {string}")
    public void getAccount(UUID id, String application, String version) {

        Response response = AccountApiClient.get(id, application, version);

        context.saveGet(response);

    }

    @When("get account for application {string} and version {string}")
    public void getAccount(String application, String version) {

        getAccount(context.lastId(), application, version);

    }

    @Then("account status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @And("account exists")
    public void checkExists() {

        getAccount(TEST_APP, VERSION_V1);

        checkStatus(200);

    }

    @And("account {string} exists")
    public void checkProperty(String property) {

        assertThat(context.lastGet().jsonPath().getString(property), is(notNullValue()));

    }

    @And("account is")
    public void checkBody(JsonNode body) throws JsonProcessingException {

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(context.lastGet().asString());
        expectedBody.remove("id");
        expectedBody.remove("creation_date");
        expectedBody.remove("update_date");

        assertThat(expectedBody, is(body));

    }

    @And("account error is")
    public void checkError(JsonNode body) throws JsonProcessingException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        Streams.stream(errors.elements()).map(ObjectNode.class::cast).forEach((ObjectNode error) -> error.remove("message"));

        assertThat(errors, is(body));

    }

}
