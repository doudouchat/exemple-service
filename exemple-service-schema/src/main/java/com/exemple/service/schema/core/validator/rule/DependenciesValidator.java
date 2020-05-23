package com.exemple.service.schema.core.validator.rule;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.everit.json.schema.Schema;
import org.everit.json.schema.Validator;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.common.exception.ValidationException;
import com.exemple.service.schema.common.exception.ValidationExceptionBuilder;
import com.exemple.service.schema.core.validator.ValidatorService;
import com.exemple.service.schema.validation.SchemaValidationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

@Component
public class DependenciesValidator implements ValidatorService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SchemaResource schemaResource;

    public DependenciesValidator(SchemaResource schemaResource) {

        this.schemaResource = schemaResource;
    }

    @Override
    public void validate(String value, JsonNode form, JsonNode old, ValidationException validationException) {

        ServiceContext context = ServiceContextExecution.context();

        String app = context.getApp();
        String version = context.getVersion();
        String resource = SchemaValidationContext.get().getResource();
        String profile = context.getProfile();

        String[] values = StringUtils.split(value, ",");
        String path = values[0];

        if (old != null && isPresent(form, values)) {

            JSONObject rawSchema = new JSONObject();
            rawSchema.put("dependencies",
                    new JSONObject().put(path,
                            ((JSONObject) new JSONObject(
                                    new JSONTokener(new ByteArrayInputStream(schemaResource.get(app, version, resource, profile).getContent())))
                                            .remove("dependencies")).get(path)));

            SchemaLoader schemaLoader = SchemaLoader.builder().draftV7Support().schemaJson(rawSchema).build();
            Schema schema = schemaLoader.load().build();
            Validator validator = Validator.builder().build();
            try {

                ObjectReader updater = MAPPER.readerForUpdating(old);
                JsonNode merged = updater.readValue(form);

                @SuppressWarnings("unchecked")
                Map<String, Object> formMap = MAPPER.convertValue(merged, Map.class);

                validator.performValidation(schema, new JSONObject(formMap));
            } catch (IOException | org.everit.json.schema.ValidationException e) {

                ValidationExceptionBuilder.buildException((org.everit.json.schema.ValidationException) e, validationException);

            }

        }
    }

    private static boolean isPresent(JsonNode form, String... values) {

        return Arrays.stream(values).map(value -> form.at("/" + value)).anyMatch(node -> !node.isMissingNode());
    }

}
