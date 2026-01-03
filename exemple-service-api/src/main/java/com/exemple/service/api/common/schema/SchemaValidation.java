package com.exemple.service.api.common.schema;

import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;

import jakarta.ws.rs.container.ContainerRequestContext;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

@RequiredArgsConstructor
public class SchemaValidation {

    private final com.exemple.service.schema.validation.SchemaValidation schema;

    private final ContainerRequestContext requestContext;

    public void validate(JsonNode source, String resourceName) {

        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schema.validate(resourceName, version, profile, source);
    }

    public void validate(ArrayNode patch, JsonNode previousSource, String resourceName) {

        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schema.validate(resourceName, version, profile, patch, previousSource);
    }

}
