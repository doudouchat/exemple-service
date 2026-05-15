package com.exemple.service.api.common.schema;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class SchemaValidationSupplier implements Supplier<SchemaValidation> {

    private final com.exemple.service.schema.validation.SchemaValidation schemaValidation;

    @Context
    private ContainerRequestContext servletContext;

    @Inject
    public SchemaValidationSupplier(com.exemple.service.schema.validation.SchemaValidation schemaValidation) {
        this.schemaValidation = schemaValidation;
    }

    @Override
    public SchemaValidation get() {
        return new SchemaValidation(schemaValidation, servletContext);
    }

}
