package com.exemple.service.api.common.schema;

import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class FilterHelper {

    private final SchemaFilter schemaFilter;

    public FilterHelper(SchemaFilter schemaFilter) {

        this.schemaFilter = schemaFilter;
    }

    public JsonNode filter(JsonNode source, String resourceName, ContainerRequestContext requestContext) {

        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        String version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        String profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schemaFilter.filter(app, version, resourceName, profile, source);
    }

}
