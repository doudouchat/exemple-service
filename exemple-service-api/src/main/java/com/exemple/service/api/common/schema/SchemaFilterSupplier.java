package com.exemple.service.api.common.schema;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class SchemaFilterSupplier implements Supplier<SchemaFilter> {

    @Autowired
    private com.exemple.service.schema.filter.SchemaFilter schemaFilter;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public SchemaFilter get() {
        return new SchemaFilter(schemaFilter, requestContext);
    }

}
