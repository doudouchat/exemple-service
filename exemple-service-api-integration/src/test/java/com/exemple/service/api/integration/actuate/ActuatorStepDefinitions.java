package com.exemple.service.api.integration.actuate;

import static org.assertj.core.api.Assertions.assertThat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.integration.core.JsonRestTemplate;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class ActuatorStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private ActuatorTestContext context;

    @When("actuator info")
    public void info() {

        Response response = JsonRestTemplate.given().get("/actuator/info");

        context.saveGet(response);

    }

    @When("actuator health")
    public void health() {

        Response response = JsonRestTemplate.given().get("/actuator/health");

        context.saveGet(response);

    }

    @When("actuator info html")
    public void infoHtml() {

        Response response = JsonRestTemplate.given().accept(ContentType.HTML).get();

        context.saveGet(response);

    }

    @Then("actuator status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode()).isEqualTo(status);

    }

    @And("actuator contains {string}")
    public void checkProperty(String property) {

        assertThat(context.lastGet().jsonPath().getString(property)).isNotNull();

    }

    @And("actuator is")
    public void checkBody(JsonNode body) throws JsonProcessingException {

        ObjectNode expectedBody = (ObjectNode) MAPPER.readTree(context.lastGet().asString());

        assertThat(expectedBody).isEqualTo(body);

    }

    @And("actuator html contains {string}")
    public void checkHtmlProperty(String property) {

        Document doc = Jsoup.parse(context.lastGet().getBody().print());
        assertThat(doc.getElementById(property).text()).isNotNull();

    }

}
