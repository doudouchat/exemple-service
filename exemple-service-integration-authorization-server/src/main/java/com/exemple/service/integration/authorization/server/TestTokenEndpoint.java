package com.exemple.service.integration.authorization.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.StringNode;

@RestController
@RequiredArgsConstructor
public class TestTokenEndpoint {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final TestAlgorithmConfiguration testAlgorithmConfiguration;

    @GetMapping(value = "/oauth/token_key")
    public JsonNode tokenKey() {

        var tokenKey = MAPPER.createObjectNode();
        tokenKey.set("value", StringNode.valueOf(String.valueOf(testAlgorithmConfiguration.getPublicKeyContent())));

        return tokenKey;

    }
}
