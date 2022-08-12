package com.exemple.service.api.common.schema;

import javax.ws.rs.container.ContainerRequestContext;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SchemaValidation {

    private final com.exemple.service.schema.validation.SchemaValidation schema;

    private final ContainerRequestContext requestContext;

    public void validate(JsonNode source, String resourceName) {

        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schema.validate(app, version, profile, resourceName, source);
    }

    public void validate(JsonNode source, JsonNode previousSource, String resourceName) {

        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schema.validate(app, version, profile, resourceName, source, previousSource);
    }

    public void validate(ArrayNode patch, JsonNode previousSource, String resourceName) {

        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        var version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        var profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schema.validate(app, version, profile, resourceName, patch, previousSource);
    }

}
