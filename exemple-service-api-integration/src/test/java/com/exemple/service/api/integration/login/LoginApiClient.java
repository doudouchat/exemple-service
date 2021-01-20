package com.exemple.service.api.integration.login;

import static com.exemple.service.api.integration.core.InitData.APP_HEADER;
import static com.exemple.service.api.integration.core.InitData.VERSION_HEADER;

import java.util.UUID;

import com.exemple.service.api.integration.core.JsonRestTemplate;

import io.restassured.response.Response;

public final class LoginApiClient {

    public static final String LOGIN_URL = "/ws/v1/logins";

    private LoginApiClient() {

    }

    public static Response post(Object body, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .body(body).post(LOGIN_URL);

    }

    public static Response get(String username, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response get(UUID id, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .get(LOGIN_URL + "/id/{id}", id);

    }

    public static Response head(Object login, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .head(LOGIN_URL + "/{login}", login);

    }

    public static Response delete(Object login, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .delete(LOGIN_URL + "/{login}", login);

    }

    public static Response patch(String login, Object patchs, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .body(patchs).patch(LOGIN_URL + "/{login}", login);

    }

    public static Response patch(UUID id, Object patchs, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .body(patchs).patch(LOGIN_URL + "/id/{id}", id);

    }

    public static Response put(Object login, Object patchs, String application, String version) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application).header(VERSION_HEADER, version)

                .body(patchs).put(LOGIN_URL + "/{login}", login);

    }

}
