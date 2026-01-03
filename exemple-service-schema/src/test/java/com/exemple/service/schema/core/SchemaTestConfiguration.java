package com.exemple.service.schema.core;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;

import jakarta.validation.Validator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Configuration
@Import(SchemaConfiguration.class)
public class SchemaTestConfiguration {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    public SchemaResource schemaResource() throws IOException {
        SchemaResource resource = Mockito.mock(SchemaResource.class);

        SchemaEntity unknownResourceSchema = new SchemaEntity();
        Mockito.when(resource.get("schema_test", "unknown", "unknown")).thenReturn(Optional.of(unknownResourceSchema));

        SchemaEntity schemaTest = new SchemaEntity();
        schemaTest.setContent(MAPPER.readTree(new ClassPathResource("schema_test.json").getContentAsByteArray()));

        ObjectNode patchExternalId = MAPPER.createObjectNode();
        patchExternalId.put("op", "add");
        patchExternalId.put("path", "/properties/external_id/readOnly");
        patchExternalId.put("value", true);

        ObjectNode patchUpdateDate = MAPPER.createObjectNode();
        patchUpdateDate.put("op", "add");
        patchUpdateDate.put("path", "/properties/update_date");
        patchUpdateDate.set("value", MAPPER.readTree(
                """
                {"type": "string","format": "date-time","readOnly": true}
                """));

        schemaTest.setPatchs(Set.of(patchExternalId, patchUpdateDate));
        Mockito.when(resource.get("schema_test", "default", "default")).thenReturn(Optional.of(schemaTest));

        SchemaEntity schemaArray = new SchemaEntity();
        schemaArray.setContent(MAPPER.readTree(new ClassPathResource("schema_array.json").getContentAsByteArray()));

        Mockito.when(resource.get("array_test", "default", "default")).thenReturn(Optional.of(schemaArray));

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
