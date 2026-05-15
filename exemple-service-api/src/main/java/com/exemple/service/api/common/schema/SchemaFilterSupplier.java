package com.exemple.service.api.common.schema;

import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class SchemaFilterSupplier implements Supplier<SchemaFilter> {

    private final com.exemple.service.schema.filter.SchemaFilter schemaFilter;

    @Context
    private ContainerRequestContext requestContext;

    @Inject
    public SchemaFilterSupplier(com.exemple.service.schema.filter.SchemaFilter schemaFilter) {
        this.schemaFilter = schemaFilter;
    }

    @Override
    public SchemaFilter get() {
        return new SchemaFilter(schemaFilter, requestContext);
    }

}
