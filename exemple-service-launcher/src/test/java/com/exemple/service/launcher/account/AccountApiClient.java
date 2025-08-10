package com.exemple.service.launcher.account;

import static com.exemple.service.launcher.core.InitData.APP_HEADER;
import static com.exemple.service.launcher.core.InitData.VERSION_HEADER;

import com.exemple.service.launcher.core.JsonRestTemplate;

import io.restassured.response.Response;

public final class AccountApiClient {

    public static final String ACCOUNT_URL = "/ws/v1/accounts";

    private AccountApiClient() {

    }

    public static Response post(Object body, String application, String version, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header(VERSION_HEADER, version).header("Authorization", token)
                .body(body).post(ACCOUNT_URL);

    }

    public static Response get(Object id, String application, String version, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header(VERSION_HEADER, version).header("Authorization", token)
                .get(ACCOUNT_URL + "/{id}", id);

    }

    public static Response patch(Object id, Object patchs, String application, String version, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header(VERSION_HEADER, version).header("Authorization", token)
                .body(patchs).patch(ACCOUNT_URL + "/{id}", id);

    }

    public static Response put(Object id, Object patchs, String application, String version, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header(VERSION_HEADER, version).header("Authorization", token)
                .body(patchs).put(ACCOUNT_URL + "/{id}", id);

    }

}
