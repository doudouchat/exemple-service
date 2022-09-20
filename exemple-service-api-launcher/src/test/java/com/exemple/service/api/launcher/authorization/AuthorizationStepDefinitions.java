package com.exemple.service.api.launcher.authorization;

import org.apache.kafka.common.Uuid;
import org.springframework.beans.factory.annotation.Autowired;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import io.cucumber.java.en.Given;

public class AuthorizationStepDefinitions {

    @Autowired
    private AuthorizationTestContext context;

    @Autowired
    private Algorithm algo;

    @Given("get authorization to create account for client {string}")
    public void authorizationCreateAccount(String client) {

        String token = JWT.create()
                .withArrayClaim("scope", new String[] { "account:create", "login:head" })
                .withClaim("client_id", client)
                .withJWTId(Uuid.randomUuid().toString())
                .sign(algo);

        context.saveAccessToken(token);

    }

    @Given("get authorization to read & update stock for client {string}")
    public void authorizationStock(String client) {

        String token = JWT.create()
                .withArrayClaim("scope", new String[] { "stock:read", "stock:update" })
                .withClaim("client_id", client)
                .withJWTId(Uuid.randomUuid().toString())
                .sign(algo);

        context.saveAccessToken(token);

    }

    @Given("get authorization to read & update subscription for client {string}")
    public void authorizationSubscription(String client) {

        String token = JWT.create()
                .withArrayClaim("scope", new String[] { "subscription:read", "subscription:update" })
                .withClaim("client_id", client)
                .withJWTId(Uuid.randomUuid().toString())
                .sign(algo);

        context.saveAccessToken(token);

    }

    @Given("get authorization from account {string} and client {string}")
    public void authorizationAccount(String username, String client) {

        String token = JWT.create()
                .withArrayClaim("scope", new String[] { "account:read", "account:update", "login:read", "login:head" })
                .withClaim("client_id", client)
                .withSubject(username)
                .withJWTId(Uuid.randomUuid().toString())
                .sign(algo);

        context.saveAccessToken(token);

    }

}
