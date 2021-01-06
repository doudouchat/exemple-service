package com.exemple.service.api.integration.swagger;

import static com.exemple.service.api.integration.core.IntegrationTestConfiguration.TEST_APP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.core.IntegrationTestConfiguration;
import com.exemple.service.api.integration.core.JsonRestTemplate;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.restassured.response.Response;

@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public class SwaggerIT extends AbstractTestNGSpringContextTests {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private HazelcastInstance cache;

    @Test
    public void swagger() {

        Response response = JsonRestTemplate.given().get("/ws/" + TEST_APP + "/openapi.json");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

    @DataProvider(name = "schema")
    private static Object[][] schema() {

        return new Object[][] { { TEST_APP }, { "other" } };
    }

    @Test(dataProvider = "schema")
    public void schema(String app) throws IOException {

        Response response = JsonRestTemplate.given().get("/ws/v1/schemas/account/" + app + "/v1/user");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

        IMap<SimpleKey, SchemaEntity> schema = cache.getMap("schema_resource");
        assertThat(schema.get(new SimpleKey(app, "v1", "account", "user")).getContent(), is(MAPPER.readTree(response.getBody().asString())));

    }

    @Test
    public void patch() {

        Response response = JsonRestTemplate.given().get("/ws/v1/schemas/patch");

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));

    }

}
