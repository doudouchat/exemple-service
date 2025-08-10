package com.exemple.service.launcher.login;

import static com.exemple.service.launcher.core.InitData.APP_HEADER;

import com.exemple.service.launcher.core.JsonRestTemplate;

import io.restassured.response.Response;

public final class LoginApiClient {

    public static final String LOGIN_URL = "/ws/v1/logins";

    private LoginApiClient() {

    }

    public static Response get(String username, String application, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header("Authorization", token)
                .get(LOGIN_URL + "/{username}", username);

    }

    public static Response head(Object login, String application, String token) {

        return JsonRestTemplate.given()
                .header(APP_HEADER, application).header("Authorization", token)
                .head(LOGIN_URL + "/{login}", login);

    }

}
