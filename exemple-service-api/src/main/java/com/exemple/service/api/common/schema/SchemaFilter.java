package com.exemple.service.api.common.schema;

import javax.ws.rs.container.ContainerRequestContext;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchemaFilter {

    private final com.exemple.service.schema.filter.SchemaFilter schema;

    private final ContainerRequestContext requestContext;

    public JsonNode filter(JsonNode source, String resourceName) {

        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        String version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        String profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schema.filter(app, version, resourceName, profile, source);
    }

    public JsonNode filterAllAdditionalProperties(JsonNode source, String resourceName) {

        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        String version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        String profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schema.filterAllAdditionalProperties(app, version, resourceName, profile, source);

    }

}
