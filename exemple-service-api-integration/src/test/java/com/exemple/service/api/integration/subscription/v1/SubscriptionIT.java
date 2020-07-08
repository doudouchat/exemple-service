package com.exemple.service.api.integration.subscription.v1;

import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.APP_HEADER_VALUE;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER;
import static com.exemple.service.api.integration.account.v1.AccountNominalIT.VERSION_HEADER_VALUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Collections;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.testng.annotations.Test;

import com.exemple.service.api.integration.account.v1.AccountNominalIT;
import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public class SubscriptionIT {

    public static final String URL = "/ws/v1/subscriptions";

    private static final String EMAIL = UUID.randomUUID().toString() + "@gmail.com";

    @Test
    public void createSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(Collections.emptyMap()).put(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.CREATED.value()));

    }

    @Test(dependsOnMethods = "createSubscription")
    public void readSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .get(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
        assertThat(response.jsonPath().getString("email"), is(EMAIL));

    }

    @Test(dependsOnMethods = "createSubscription")
    public void updateSubscription() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(Collections.emptyMap()).put(URL + "/{email}", EMAIL);

        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT.value()));

    }

    @Test
    public void createSubscriptionFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, "v1")

                .body(Collections.emptyMap()).put(URL + "/{email}", AccountNominalIT.ACCOUNT_BODY.get("email"));

        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST.value()));
        assertThat(response.jsonPath().getList("code").get(0), is("login"));
        assertThat(response.jsonPath().getList("path").get(0), is("/email"));

    }

    @Test
    public void readSubscriptionFailure() {

        Response response = JsonRestTemplate.given()

                .header(APP_HEADER, APP_HEADER_VALUE).header(VERSION_HEADER, VERSION_HEADER_VALUE)

                .get(URL + "/{email}", AccountNominalIT.ACCOUNT_BODY.get("email"));

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND.value()));
    }
}
