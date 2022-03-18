package com.exemple.service.api.common.schema;

import javax.ws.rs.container.ContainerRequestContext;

import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.schema.merge.SchemaMerge;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaHelper {

    private final SchemaMerge schemaMerge;

    public void complete(JsonNode source, JsonNode orignalSource, String resourceName, ContainerRequestContext requestContext) {

        String app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        String version = requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER);
        String profile = ((ApiSecurityContext) requestContext.getSecurityContext()).getProfile();

        schemaMerge.mergeMissingFieldFromOriginal(app, version, resourceName, profile, source, orignalSource);

    }

}
