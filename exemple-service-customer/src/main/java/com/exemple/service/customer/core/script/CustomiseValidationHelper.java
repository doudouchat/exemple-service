package com.exemple.service.customer.core.script;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.CustomerScriptFactory;
import com.exemple.service.customer.common.script.CustomiseValidation;
import com.exemple.service.customer.common.script.validation.CustomiseValidationImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomiseValidationHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String BEAN_NAME = "ValidationCustom";

    private final CustomerScriptFactory customerScriptFactory;

    public CustomiseValidationHelper(CustomerScriptFactory customerScriptFactory) {

        this.customerScriptFactory = customerScriptFactory;
    }

    public void validate(String sourceName, JsonNode source) {

        validate(source, buildCustomiseValidation(sourceName)::validate);

    }

    public void validate(String sourceName, JsonNode source, JsonNode previousSource) {

        validate(source, previousSource, buildCustomiseValidation(sourceName)::validate);

    }

    private CustomiseValidation buildCustomiseValidation(String resource) {
        return new CustomiseValidationImpl(resource + BEAN_NAME, customerScriptFactory);
    }

    private static void validate(JsonNode source, Consumer<Map<String, Object>> service) {

        @SuppressWarnings("unchecked")
        Map<String, Object> formMap = MAPPER.convertValue(source, Map.class);
        service.accept(formMap);
    }

    private static void validate(JsonNode source1, JsonNode source2, BiConsumer<Map<String, Object>, Map<String, Object>> service) {

        @SuppressWarnings("unchecked")
        Map<String, Object> formMap = MAPPER.convertValue(source1, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> oldMap = MAPPER.convertValue(source2, Map.class);
        service.accept(formMap, oldMap);
    }

}
