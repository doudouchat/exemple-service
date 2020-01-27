package com.exemple.service.schema.core;

import java.io.IOException;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.impl.SchemaResourceImpl;
import com.exemple.service.resource.schema.model.SchemaEntity;

@Configuration
public class SchemaTestConfiguration extends SchemaConfiguration {

    private Resource schema = new ClassPathResource("schema_test.json");

    @Bean
    public SchemaResource schemaResource() throws IOException {
        SchemaResource resource = Mockito.mock(SchemaResource.class);

        SchemaEntity unknownResourceSchema = new SchemaEntity();
        unknownResourceSchema.setContent(SchemaResourceImpl.SCHEMA_DEFAULT.getBytes());
        Mockito.when(resource.get(Mockito.eq("unknown"), Mockito.eq("unknown"), Mockito.eq("schema_test"))).thenReturn(unknownResourceSchema);

        SchemaEntity defaultResourceSchema = new SchemaEntity();
        defaultResourceSchema.setContent(IOUtils.toByteArray(schema.getInputStream()));
        defaultResourceSchema.setRules(Collections.singletonMap("dependencies", Collections.singleton("opt_in_email")));
        Mockito.when(resource.get(Mockito.eq("default"), Mockito.eq("default"), Mockito.eq("schema_test"))).thenReturn(defaultResourceSchema);

        return resource;
    }

}
