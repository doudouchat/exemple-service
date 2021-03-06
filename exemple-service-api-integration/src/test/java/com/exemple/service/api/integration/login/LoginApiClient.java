package com.exemple.service.api.integration.login;

import static com.exemple.service.api.integration.core.InitData.APP_HEADER;
import static com.exemple.service.api.integration.core.InitData.VERSION_HEADER;

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
    
    public static Response head(Object login, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .head(LOGIN_URL + "/{login}", login);

    }

    public static Response delete(Object username, String application) {

        return JsonRestTemplate.given()

                .header(APP_HEADER, application)

                .delete(LOGIN_URL + "/{username}", username);

    }

}
