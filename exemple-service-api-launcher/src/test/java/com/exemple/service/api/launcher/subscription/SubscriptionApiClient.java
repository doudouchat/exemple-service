package com.exemple.service.api.launcher.subscription;

import static com.exemple.service.api.launcher.core.InitData.APP_HEADER;
import static com.exemple.service.api.launcher.core.InitData.VERSION_HEADER;

import java.util.Collections;

import com.exemple.service.api.launcher.core.JsonRestTemplate;

import io.restassured.response.Response;

public final class SubscriptionApiClient {

    public static final String SUBSCRIPTION_URL = "/ws/v1/subscriptions";

    private SubscriptionApiClient() {

    }

    public static Response put(String email, Object body, String application, String version, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header(VERSION_HEADER, version).header("Authorization", token)
                .body(Collections.emptyMap()).put(SUBSCRIPTION_URL + "/{email}", email);

    }

    public static Response get(String email, String application, String version, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header(VERSION_HEADER, version).header("Authorization", token)
                .get(SUBSCRIPTION_URL + "/{email}", email);

    }

}
