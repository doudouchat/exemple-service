package com.exemple.service.integration.authorization.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TestTokenEndpoint {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TestAlgorithmConfiguration testAlgorithmConfiguration;

    @GetMapping(value = "/oauth/token_key")
    public JsonNode tokenKey() {

        var tokenKey = MAPPER.createObjectNode();
        tokenKey.put("value", String.valueOf(testAlgorithmConfiguration.getPublicKeyContent()));

        return tokenKey;

    }
}
