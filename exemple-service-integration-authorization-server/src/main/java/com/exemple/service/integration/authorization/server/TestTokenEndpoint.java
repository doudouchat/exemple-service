package com.exemple.service.integration.authorization.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class TestTokenEndpoint {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final byte[] publicKeyContent;

    public TestTokenEndpoint(@Value("${public-key-location:classpath:public_key}") Resource publicKeyResource) throws IOException {

        this.publicKeyContent = IOUtils.toByteArray(publicKeyResource.getInputStream());
    }

    @GetMapping(value = "/oauth/token_key")
    public JsonNode tokenKey() {

        var tokenKey = MAPPER.createObjectNode();
        tokenKey.put("value", new String(publicKeyContent, StandardCharsets.UTF_8));

        return tokenKey;

    }
}
