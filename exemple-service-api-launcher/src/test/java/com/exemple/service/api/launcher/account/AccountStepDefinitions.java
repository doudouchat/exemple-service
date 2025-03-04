package com.exemple.service.api.launcher.account;

import static com.exemple.service.api.launcher.core.InitData.TEST_APP;
import static com.exemple.service.api.launcher.core.InitData.VERSION_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.condition.AnyOf.anyOf;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.assertj.core.api.Condition;
import org.assertj.core.util.Streams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.exemple.service.api.launcher.authorization.AuthorizationTestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

    @Autowired
    private KafkaConsumer<String, JsonNode> consumerEvent;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Given("account")
    public void buildAccount(JsonNode body) {

        Response response = AccountApiClient.post(body, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        assertThat(response.getStatusCode()).as("failure account %s", body.toPrettyString()).isEqualTo(201);

        UUID id = UUID.fromString(response.getHeader("Location").substring(response.getHeader("Location").lastIndexOf('/') + 1));
        context.saveId(id);
    }

    @When("create account for application {string} and version {string}")
    public void createAccount(String application, String version, JsonNode body) {

        Response response = AccountApiClient.post(body, application, version, authorizationContext.lastAccessToken());

        context.savePost(response);

        if (StringUtils.isNotEmpty(response.getHeader("Location"))) {
            UUID id = UUID.fromString(response.getHeader("Location").substring(response.getHeader("Location").lastIndexOf('/') + 1));
            context.saveId(id);
        }

    }

    @When("create account")
    public void createAccount(JsonNode body) {

        createAccount(TEST_APP, VERSION_V1, body);
    }

    @When("patch account")
    public void patchAccount(JsonNode body) {

        Response response = AccountApiClient.patch(context.lastId(), body, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        context.savePatch(response);

    }

    @When("put account")
    public void putAccount(JsonNode body) {

        Response response = AccountApiClient.put(context.lastId(), body, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        context.savePut(response);

    }

    @When("create any account with {string} and value {int}")
    public void createAccount(String property, int value) throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");
        Map<String, Object> body = JsonPath.parse(resource.getInputStream()).set(property, value).json();

        Response response = AccountApiClient.post(body, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        context.savePost(response);

    }

    @When("create any account with {string} and value {string}")
    public void createAccount(String property, String value) throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");

        DocumentContext account = JsonPath.parse(resource.getInputStream());
        Map<String, Object> body = account.put("$", property, value).json();

        Response response = AccountApiClient.post(body, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        context.savePost(response);

    }

    @When("create any account with {string}")
    public void createAccount(String property, JsonNode value) throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");

        DocumentContext account = JsonPath.parse(resource.getInputStream());
        Map<String, Object> body = account.put("$", property, value).json();

        Response response = AccountApiClient.post(body, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        context.savePost(response);

    }

    @When("get account for application {string} and version {string}")
    public void getAccount(String application, String version) {

        Response response = AccountApiClient.get(context.lastId(), application, version, authorizationContext.lastAccessToken());

        context.saveGet(response);

    }

    @When("get account by id {id}")
    public void getAccount(UUID id) {

        Response response = AccountApiClient.get(id, TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        context.saveGet(response);

    }

    @Given("create any account")
    public void createAccount() throws IOException {

        Resource resource = new ClassPathResource("account/nominal_account.json");

        Map<String, Object> body = JsonPath.parse(resource.getInputStream()).json();

        buildAccount(MAPPER.convertValue(body, JsonNode.class));

    }

    @And("account property {string} exists")
    public void checkProperty(String property) {

        assertThat(context.lastGet().jsonPath().getString(property)).as("account property %s not exists", property).isNotNull();

    }

    @Then("account is")
    public void getAccount(JsonNode body) throws IOException {

        assertAll(
                () -> assertThat(context.lastResponse().getStatusCode()).as("account is incorrect %s", context.lastResponse().getStatusCode())
                        .is(anyOf(
                                new Condition<>(status -> status == 204, "status"),
                                new Condition<>(status -> status == 201, "status"))),
                () -> assertThat(context.lastResponse().asString()).as("account is incorrect %s", context.lastResponse().asString()).isEmpty());

        Response response = AccountApiClient.get(context.lastId(), TEST_APP, VERSION_V1, authorizationContext.lastAccessToken());

        assertThat(response.getStatusCode()).as("account %s not exists", context.lastId()).isEqualTo(200);

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(response.asString());
        expectedBody.remove("id");
        expectedBody.remove("creation_date");
        expectedBody.remove("update_date");

        assertThat(expectedBody).isEqualTo(body);

        context.saveGet(response);

    }

    @And("account event is")
    public void getAccountEvent(JsonNode body) {

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            ConsumerRecords<String, JsonNode> records = consumerEvent.poll(Duration.ofSeconds(5));
            assertThat(records.iterator()).toIterable().last().satisfies(event -> {

                ObjectNode expectedBody = (ObjectNode) event.value();
                expectedBody.remove("creation_date");
                expectedBody.remove("update_date");
                expectedBody.remove("id");

                assertThat(expectedBody).isEqualTo(body);
            });
        });

    }

    @Then("account is denied")
    public void checkDenied() {

        assertThat(context.lastResponse().getStatusCode()).as("account is not denied").isEqualTo(403);

    }

    @And("account error only contains")
    public void checkOnlyError(JsonNode body) throws IOException {

        checkCountError(1);
        checkErrors(body);
    }

    @And("account error contains {int} errors")
    public void checkCountError(int count) throws IOException {

        assertThat(context.lastResponse().getStatusCode()).as("account has not error").isEqualTo(400);

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());

        assertThat(Streams.stream(errors)).as("errors %s not contain expected errors", errors.toPrettyString()).hasSize(count);

    }

    @And("account error contains")
    public void checkErrors(JsonNode body) throws IOException {

        ArrayNode errors = (ArrayNode) MAPPER.readTree(context.lastResponse().asString());
        assertThat(errors).as("errors %s not contain %s", errors.toPrettyString(), body.toPrettyString())
                .anySatisfy(error -> {
                    Iterator<Map.Entry<String, JsonNode>> expectedErrors = body.fields();
                    while (expectedErrors.hasNext()) {
                        Map.Entry<String, JsonNode> expectedError = expectedErrors.next();
                        assertThat(error.get(expectedError.getKey())).isEqualTo(expectedError.getValue());
                    }
                });
    }

}
