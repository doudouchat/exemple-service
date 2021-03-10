package com.exemple.service.schema.core;

import java.io.IOException;
import java.util.Collections;

import javax.validation.Validator;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.impl.SchemaResourceImpl;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Configuration
@Import(SchemaConfiguration.class)
public class SchemaTestConfiguration {

    private Resource schema = new ClassPathResource("schema_test.json");

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Bean
    public SchemaResource schemaResource() throws IOException {
        SchemaResource resource = Mockito.mock(SchemaResource.class);

        SchemaEntity unknownResourceSchema = new SchemaEntity();
        unknownResourceSchema.setContent(SchemaResourceImpl.SCHEMA_DEFAULT);
        Mockito.when(resource.get(Mockito.eq("unknown"), Mockito.eq("unknown"), Mockito.eq("schema_test"), Mockito.eq("unknown")))
                .thenReturn(unknownResourceSchema);

        SchemaEntity defaultResourceSchema = new SchemaEntity();
        defaultResourceSchema.setContent(MAPPER.readTree(IOUtils.toByteArray(schema.getInputStream())));

        ObjectNode patch = MAPPER.createObjectNode();
        patch.put("op", "add");
        patch.put("path", "/properties/external_id/readOnly");
        patch.put("value", true);

        defaultResourceSchema.setPatchs(Collections.singleton(patch));
        Mockito.when(resource.get(Mockito.eq("default"), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(defaultResourceSchema);

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
