package com.exemple.service.api.common.schema;

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

        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schema.filter(resourceName, version, profile, source);
    }

    public JsonNode filterAllAdditionalProperties(JsonNode source, String resourceName) {

        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        return schema.filterAllAdditionalProperties(resourceName, version, profile, source);

    }

}
