package com.exemple.service.schema.core;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.exemple.service.resource.core.statement.SchemaStatement;
import com.exemple.service.resource.schema.SchemaResource;

@Configuration
public class SchemaTestConfiguration extends SchemaConfiguration {

    private Resource schema = new ClassPathResource("schema_test.json");

    @Bean
    public SchemaResource schemaResource() throws IOException {
        SchemaResource resource = Mockito.mock(SchemaResource.class);
        Mockito.when(resource.get(Mockito.eq("unknown"), Mockito.eq("unknown"), Mockito.eq("schema_test")))
                .thenReturn(SchemaStatement.SCHEMA_DEFAULT.getBytes());
        Mockito.when(resource.get(Mockito.eq("default"), Mockito.eq("default"), Mockito.eq("schema_test")))
                .thenReturn(IOUtils.toByteArray(schema.getInputStream()));
        return resource;
    }

}
