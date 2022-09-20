package com.exemple.service.api.launcher.authorization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;

import org.springframework.stereotype.Component;

import io.cucumber.spring.ScenarioScope;

@Component
@ScenarioScope
public class AuthorizationTestContext {

    private final LinkedList<String> accessTokens;

    public AuthorizationTestContext() {
        this.accessTokens = new LinkedList<>();
    }

    public void saveAccessToken(String token) {
        this.accessTokens.add(token);
    }

    public String lastAccessToken() {
        assertThat(this.accessTokens).as("no access token").isNotEmpty();
        return this.accessTokens.getLast();
    }

}
