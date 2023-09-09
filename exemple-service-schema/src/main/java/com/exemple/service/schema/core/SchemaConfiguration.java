package com.exemple.service.schema.core;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.ClassPathResource;

import com.exemple.service.schema.common.SchemaBuilder;
import com.networknt.schema.JsonSchema;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = "com.exemple.service.schema")
public class SchemaConfiguration {
    @Bean
    public JsonSchema patchSchema() throws IOException {

        return SchemaBuilder.build(new ClassPathResource("json-patch.json").getInputStream());

    }

}
