package com.exemple.service.launcher.core;

import java.util.UUID;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.cucumber.java.DocStringType;
import io.cucumber.java.ParameterType;
import io.cucumber.spring.CucumberContextConfiguration;

@SpringBootTest
@CucumberContextConfiguration
@ContextConfiguration(classes = IntegrationTestConfiguration.class)
@ActiveProfiles("test")
public class CucumberConfiguration {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @DocStringType
    public JsonNode json(String content) throws JsonProcessingException {
        return MAPPER.readTree(content);
    }

    @ParameterType(".*")
    public UUID id(String id) {
        return UUID.fromString(id);
    }

}
