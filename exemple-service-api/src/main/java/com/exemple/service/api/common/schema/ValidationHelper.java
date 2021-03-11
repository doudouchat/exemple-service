package com.exemple.service.api.common.schema;

import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.schema.validation.SchemaValidation;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ValidationHelper {

    private final SchemaValidation schemaValidation;

    public ValidationHelper(SchemaValidation schemaValidation) {

        this.schemaValidation = schemaValidation;
    }

    public void validate(JsonNode source, String resourceName, ContainerRequestContext requestContext) {

        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        String version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        String profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schemaValidation.validate(app, version, profile, resourceName, source);
    }

    public void validate(JsonNode source, JsonNode previousSource, String resourceName, ContainerRequestContext requestContext) {

        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        String version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        String profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schemaValidation.validate(app, version, profile, resourceName, source, previousSource);
    }

}
