package com.exemple.service.customer.core.script;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.CustomerScriptFactory;
import com.exemple.service.customer.common.script.CustomiseResource;
import com.exemple.service.customer.common.script.resource.CustomiseResourceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CustomiseResourceHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String BEAN_NAME = "ServiceResource";

    private CustomerScriptFactory customerScriptFactory;

    public CustomiseResourceHelper(CustomerScriptFactory customerScriptFactory) {

        this.customerScriptFactory = customerScriptFactory;
    }

    public JsonNode customise(String sourceName, JsonNode source) {
        return execute(source, new CustomiseResourceImpl(sourceName + BEAN_NAME, customerScriptFactory));

    }

    public JsonNode customise(String sourceName, JsonNode source, JsonNode previousSource) {
        return execute(source, previousSource, new CustomiseResourceImpl(sourceName + BEAN_NAME, customerScriptFactory));

    }

    private static JsonNode execute(JsonNode source, CustomiseResource customiseResource) {
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
        return MAPPER.convertValue(customiseResource.create(sourceMap), JsonNode.class);
    }

    private static JsonNode execute(JsonNode source, JsonNode previousSource, CustomiseResource customiseResource) {
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> previousMap = MAPPER.convertValue(previousSource, Map.class);
        return MAPPER.convertValue(customiseResource.update(sourceMap, previousMap), JsonNode.class);
    }

}
