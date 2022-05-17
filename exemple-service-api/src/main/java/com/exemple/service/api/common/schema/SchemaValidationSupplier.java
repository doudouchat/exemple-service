package com.exemple.service.api.common.schema;

import java.util.function.Supplier;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
