package com.exemple.service.launcher.login;

import static com.exemple.service.launcher.core.InitData.TEST_APP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.launcher.account.AccountTestContext;
import com.exemple.service.launcher.authorization.AuthorizationTestContext;
import com.exemple.service.resource.core.ResourceExecutionContext;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.restassured.response.Response;

public class LoginStepDefinitions {

    @Autowired
    private AccountResource accountResource;

    @Autowired
    private AccountTestContext context;

    @Autowired
    private AuthorizationTestContext authorizationContext;

    @Before
    public void initKeyspace() {

        ResourceExecutionContext.get().setKeyspace("test_keyspace");

    }

    @Given("delete username {string}")
    public void remove(String username) {
        accountResource.removeByUsername("email", username);

    }

    @And("get id account {string}")
    public void getLogin(String username) {

        Response response = LoginApiClient.get(username, TEST_APP, authorizationContext.lastAccessToken());

        assertAll(
                () -> assertThat(response.getStatusCode()).as("login %s not found", username).isEqualTo(200),
                () -> assertThat(response.as(UUID.class)).as("login id and creation id not match", username).isEqualTo(context.lastId()));

        context.saveId(response.as(UUID.class));

    }

    @And("account {string} exists")
    public void checkExists(String username) {

        Response response = LoginApiClient.head(username, TEST_APP, authorizationContext.lastAccessToken());

        assertThat(response.getStatusCode()).as("login %s not exists", username).isEqualTo(204);

    }

    @And("account {string} not exists")
    public void checkNotExists(String username) {

        Response response = LoginApiClient.head(username, TEST_APP, authorizationContext.lastAccessToken());

        assertThat(response.getStatusCode()).as("login %s exists", username).isEqualTo(404);

    }

}
