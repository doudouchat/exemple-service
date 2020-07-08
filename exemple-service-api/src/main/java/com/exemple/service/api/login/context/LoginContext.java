package com.exemple.service.api.login.context;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import com.fasterxml.jackson.databind.JsonNode;

@Component
@RequestScope
public class LoginContext {

    private final Map<String, JsonNode> logins = new HashMap<>();

    public JsonNode getLogin(String username) {
        return logins.get(username);
    }

    public void setLogin(String username, JsonNode login) {
        this.logins.put(username, login);
    }

}
