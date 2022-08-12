package com.exemple.service.schema.core;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Configuration
@Import(SchemaConfiguration.class)
public class SchemaTestConfiguration {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    public SchemaResource schemaResource() throws IOException {
        SchemaResource resource = Mockito.mock(SchemaResource.class);

        SchemaEntity unknownResourceSchema = new SchemaEntity();
        Mockito.when(resource.get("unknown", "unknown", "schema_test", "unknown")).thenReturn(Optional.of(unknownResourceSchema));

        SchemaEntity schemaTest = new SchemaEntity();
        schemaTest.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("schema_test.json").getInputStream())));

        ObjectNode patchExternalId = MAPPER.createObjectNode();
        patchExternalId.put("op", "add");
        patchExternalId.put("path", "/properties/external_id/readOnly");
        patchExternalId.put("value", true);

        ObjectNode patchUpdateDate = MAPPER.createObjectNode();
        patchUpdateDate.put("op", "add");
        patchUpdateDate.put("path", "/properties/update_date");
        patchUpdateDate.set("value", MAPPER.readTree("{\"type\": \"string\",\"format\": \"date-time\",\"readOnly\": true}"));

        Set<JsonNode> schemaPatchs = new HashSet<>();
        schemaPatchs.add(patchExternalId);
        schemaPatchs.add(patchUpdateDate);

        schemaTest.setPatchs(schemaPatchs);
        Mockito.when(resource.get("default", "default", "schema_test", "default")).thenReturn(Optional.of(schemaTest));

        SchemaEntity schemaArray = new SchemaEntity();
        schemaArray.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("schema_array.json").getInputStream())));

        Mockito.when(resource.get("default", "default", "array_test", "default")).thenReturn(Optional.of(schemaArray));

        return resource;
    }

    @Bean
    public Validator validator() {

        return new LocalValidatorFactoryBean();
    }

    @Bean
    public MethodValidationPostProcessor methodValidationPostProcessor() {

        MethodValidationPostProcessor methodValidationPostProcessor = new MethodValidationPostProcessor();
        methodValidationPostProcessor.setValidator(validator());

        return methodValidationPostProcessor;
    }

}
