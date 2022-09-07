package com.exemple.service.api.common.schema;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchemaFilter {

    private final com.exemple.service.schema.filter.SchemaFilter schema;

    private final ContainerRequestContext requestContext;

    public JsonNode filter(JsonNode source, String resourceName) {

        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schema.filter(app, version, resourceName, profile, source);
    }

    public JsonNode filterAllAdditionalProperties(JsonNode source, String resourceName) {

        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schema.filterAllAdditionalProperties(app, version, resourceName, profile, source);

    }

}
