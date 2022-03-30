package com.exemple.service.api.integration.login;

import static com.exemple.service.api.integration.core.InitData.TEST_APP;
import static com.exemple.service.api.integration.core.InitData.VERSION_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.integration.account.AccountTestContext;
import com.exemple.service.customer.login.LoginResource;
import com.exemple.service.resource.core.ResourceExecutionContext;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

public class LoginStepDefinitions {

    @Autowired
    private LoginResource loginResource;

    @Autowired
    private AccountTestContext context;

    @Before
    public void initKeyspace() {

        ResourceExecutionContext.get().setKeyspace("test_keyspace");

    }

    @Given("delete username {string}")
    public void remove(String username) {

        loginResource.delete(username);

    }

    @When("get id account {string}")
    public void getLogin(String username) {

        Response response = LoginApiClient.get(username, TEST_APP, VERSION_V1);

        assertAll(
                () -> assertThat(response.getStatusCode()).isEqualTo(200),
                () -> assertThat(response.as(UUID.class)).isEqualTo(context.lastId()));

        context.saveId(response.as(UUID.class));

    }

    @And("account {string} exists")
    public void checkExists(String username) {

        Response response = LoginApiClient.head(username, TEST_APP);

        assertThat(response.getStatusCode()).isEqualTo(204);

    }

    @And("account {string} not exists")
    public void checkNotExists(String username) {

        Response response = LoginApiClient.head(username, TEST_APP);

        assertThat(response.getStatusCode()).isEqualTo(404);

    }

}
