package com.exemple.service.api.launcher.authorization;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import io.cucumber.java.en.Given;

public class AuthorizationStepDefinitions {

    @Autowired
    private AuthorizationTestContext context;

    @Autowired
    private JWSSigner signer;

    @Given("get authorization to create account for client {string}")
    public void authorizationCreateAccount(String client) throws JOSEException {

        var payload = new JWTClaimsSet.Builder()
                .claim("scope", new String[] { "account:create", "login:head" })
                .claim("client_id", client)
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(signer);

        context.saveAccessToken(token.serialize());

    }

    @Given("get authorization to read & update stock for client {string}")
    public void authorizationStock(String client) throws JOSEException {

        var payload = new JWTClaimsSet.Builder()
                .claim("scope", new String[] { "stock:read", "stock:update" })
                .claim("client_id", client)
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(signer);

        context.saveAccessToken(token.serialize());

    }

    @Given("get authorization to read & update subscription for client {string}")
    public void authorizationSubscription(String client) throws JOSEException {

        var payload = new JWTClaimsSet.Builder()
                .claim("scope", new String[] { "subscription:read", "subscription:update" })
                .claim("client_id", client)
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(signer);

        context.saveAccessToken(token.serialize());

    }

    @Given("get authorization from account {string} and client {string}")
    public void authorizationAccount(String username, String client) throws JOSEException {

        var payload = new JWTClaimsSet.Builder()
                .claim("scope", new String[] { "account:read", "account:update", "login:read", "login:head" })
                .claim("client_id", client)
                .subject(username)
                .jwtID(UUID.randomUUID().toString())
                .build();

        var token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).build(), payload);
        token.sign(signer);

        context.saveAccessToken(token.serialize());

    }

}
