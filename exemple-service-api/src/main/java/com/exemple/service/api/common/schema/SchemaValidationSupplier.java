package com.exemple.service.api.common.schema;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class SchemaValidationSupplier implements Supplier<SchemaValidation> {

    @Autowired
    private com.exemple.service.schema.validation.SchemaValidation schemaValidation;

    @Context
    private ContainerRequestContext servletContext;

    @Override
    public SchemaValidation get() {
        return new SchemaValidation(schemaValidation, servletContext);
    }

}
