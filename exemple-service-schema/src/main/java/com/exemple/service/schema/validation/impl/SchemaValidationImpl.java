package com.exemple.service.schema.validation.impl;

import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.everit.json.schema.ReadWriteContext;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionBuilder;
import com.exemple.service.schema.core.validator.ValidatorService;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

@Component
public class SchemaValidationImpl implements SchemaValidation {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private SchemaResource schemaResource;

    @Autowired
    private Schema patchSchema;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void validate(String app, String version, String resource, JsonNode form, JsonNode old) {

        JSONObject rawSchema = new JSONObject(new JSONTokener(new ByteArrayInputStream(schemaResource.get(app, version, resource).getContent())));

        @SuppressWarnings("unchecked")
        Map<String, Object> formMap = MAPPER.convertValue(form, Map.class);

        if (old != null) {

            rawSchema.remove("dependencies");
            JSONArray required = (JSONArray) rawSchema.remove("required");
            if (required != null) {
                rawSchema.put("required", required.toList().stream().filter(formMap::containsKey).collect(Collectors.toList()));
            }
        }

        SchemaLoader schemaLoader = SchemaLoader.builder().draftV7Support().schemaJson(rawSchema).build();

        Schema schema = schemaLoader.load().build();

        Validator validator = Validator.builder().readWriteContext(ReadWriteContext.WRITE).build();
        ValidationException validationException = new ValidationException();
        try {
            validator.performValidation(schema, new JSONObject(formMap));
        } catch (org.everit.json.schema.ValidationException e) {

            ValidationExceptionBuilder.buildException(e, validationException);

        }

        Map<String, Set<String>> rules = schemaResource.get(app, version, resource).getRules();
        rules.entrySet().forEach(rule -> rule.getValue().forEach((String p) -> {
            ValidatorService validatorService = applicationContext.getBean(rule.getKey().concat("Validator"), ValidatorService.class);
            validatorService.validate(p, form, old, validationException);
        }));

        if (!validationException.getAllExceptions().isEmpty()) {
            throw validationException;

        }
    }

    @Override
    public void validate(Schema schema, JsonNode target) {

        @SuppressWarnings("unchecked")
        Map<String, Object> formMap = MAPPER.convertValue(target, Map.class);

        try {
            schema.validate(new JSONObject(formMap));

        } catch (org.everit.json.schema.ValidationException e) {

            ValidationException validationException = new ValidationException(e);
            ValidationExceptionBuilder.buildException(e, validationException);

            throw validationException;
        }

    }

    @Override
    public void validatePatch(ArrayNode patch) {

        JSONArray jsonArray = new JSONArray();
        JsonNodeUtils.stream(patch.elements()).forEach(n -> jsonArray.put(MAPPER.convertValue(n, Map.class)));

        try {
            patchSchema.validate(jsonArray);

        } catch (org.everit.json.schema.ValidationException e) {

            ValidationException validationException = new ValidationException(e);
            ValidationExceptionBuilder.buildException(e, validationException);

            throw validationException;
        }

    }

}
