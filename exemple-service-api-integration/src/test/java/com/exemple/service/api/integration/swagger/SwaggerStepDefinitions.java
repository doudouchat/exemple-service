package com.exemple.service.api.integration.swagger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.SimpleKey;

import com.exemple.service.api.integration.core.JsonRestTemplate;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class SwaggerStepDefinitions {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SwaggerTestContext context;

    @Autowired
    private HazelcastInstance cache;

    @When("get swagger of application {string}")
    public void swagger(String application) {

        Response response = JsonRestTemplate.given().get("/ws/" + application + "/openapi.json");

        context.saveGet(response);

    }

    @When("get schema of application {string}")
    public void schema(String application) {

        Response response = JsonRestTemplate.given().get("/ws/v1/schemas/account/" + application + "/v1/user");

        context.saveGet(response);

    }

    @When("get schema patch")
    public void patch() {

        Response response = JsonRestTemplate.given().get("/ws/v1/schemas/patch");

        context.saveGet(response);

    }

    @Then("schema status is {int}")
    public void checkStatus(int status) {

        assertThat(context.lastResponse().getStatusCode(), is(status));

    }

    @Then("schema of application {string} is cached in keyspace {string}")
    public void checkCache(String application, String keyspace) throws JsonProcessingException {

        IMap<SimpleKey, SchemaEntity> schema = cache.getMap("schema_resource");
        assertThat(schema.get(new SimpleKey(keyspace, application, "v1", "account", "user")), is(notNullValue()));
        assertThat(schema.get(new SimpleKey(keyspace, application, "v1", "account", "user")).getContent(),
                is(MAPPER.readTree(context.lastGet().getBody().asString())));

    }

}
