package com.exemple.service.schema.core;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
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
import com.exemple.service.resource.schema.impl.SchemaResourceImpl;
import com.exemple.service.resource.schema.model.SchemaEntity;
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
        unknownResourceSchema.setContent(SchemaResourceImpl.SCHEMA_DEFAULT);
        Mockito.when(resource.get(Mockito.eq("unknown"), Mockito.eq("unknown"), Mockito.eq("schema_test"), Mockito.eq("unknown")))
                .thenReturn(unknownResourceSchema);

        SchemaEntity schemaTest = new SchemaEntity();
        schemaTest.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("schema_test.json").getInputStream())));

        ObjectNode patch = MAPPER.createObjectNode();
        patch.put("op", "add");
        patch.put("path", "/properties/external_id/readOnly");
        patch.put("value", true);

        schemaTest.setPatchs(Collections.singleton(patch));
        Set<String> schemaTestField = new HashSet<>();
        schemaTestField.add("id");
        schemaTestField.add("email");
        schemaTestField.add("external_id");
        schemaTestField.add("civility");
        schemaTestField.add("lastname");
        schemaTestField.add("firstname");
        schemaTestField.add("password");
        schemaTestField.add("birthday");
        schemaTestField.add("creation_date");
        schemaTestField.add("opt_in_email");
        schemaTestField.add("addresses[*[city,street]]");
        schemaTestField.add("cgus[code,version]");
        schemaTestField.add("cgvs[code,version]");
        schemaTest.setFields(schemaTestField);
        Mockito.when(resource.get(Mockito.eq("default"), Mockito.eq("default"), Mockito.eq("schema_test"), Mockito.eq("default")))
                .thenReturn(schemaTest);
       
        SchemaEntity schemaArray = new SchemaEntity();
        Set<String> schemaArrayField = new HashSet<>();
        schemaArrayField.add("items[city,street]");
        schemaArray.setFields(schemaArrayField);
        schemaArray.setContent(MAPPER.readTree(IOUtils.toByteArray(new ClassPathResource("schema_array.json").getInputStream())));

        Mockito.when(resource.get(Mockito.eq("default"), Mockito.eq("default"), Mockito.eq("array_test"), Mockito.eq("default")))
                .thenReturn(schemaArray);

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
