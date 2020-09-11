package com.exemple.service.customer.core.script.validation;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.core.script.CustomerScriptFactory;
import com.exemple.service.customer.core.script.CustomiseValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class SchemaValidationAspect {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String BEAN_NAME = "ValidationCustom";

    private final CustomerScriptFactory customerScriptFactory;

    public SchemaValidationAspect(CustomerScriptFactory customerScriptFactory) {

        this.customerScriptFactory = customerScriptFactory;
    }

    @After("execution(public void com.exemple.service.schema.validation.SchemaValidation.validate(.., *, *)) && args(.., resource, form)")
    public void validate(String resource, JsonNode form) {

        validate(form, buildCustomiseValidation(resource)::validate);

    }

    @After("execution(public void com.exemple.service.schema.validation.SchemaValidation.validate(.., *, *, *)) && args(.., resource, form, old)")
    public void validate(String resource, JsonNode form, JsonNode old) {

        validate(form, old, buildCustomiseValidation(resource)::validate);

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
