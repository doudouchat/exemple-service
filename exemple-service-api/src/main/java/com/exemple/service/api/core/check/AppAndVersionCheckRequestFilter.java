package com.exemple.service.api.core.check;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;

import com.exemple.service.api.common.model.SchemaBeanParam;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Priority(Priorities.AUTHORIZATION)
@RequiredArgsConstructor
public class AppAndVersionCheckRequestFilter implements ContainerRequestFilter {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final boolean optionalVersion;

    private final MessageSource messageSource;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        var entity = MAPPER.createObjectNode();

        if (StringUtils.isBlank(requestContext.getHeaderString(SchemaBeanParam.APP_HEADER))) {

            entity.put(SchemaBeanParam.APP_HEADER, getMessage(requestContext));
            var response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(entity)
                    .build();
            requestContext.abortWith(response);

        }

        if (!optionalVersion && StringUtils.isBlank(requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER))) {

            entity.put(SchemaBeanParam.VERSION_HEADER, getMessage(requestContext));
            var response = Response.status(Response.Status.BAD_REQUEST)
                    .entity(entity)
                    .build();
            requestContext.abortWith(response);

        }

    }

    private String getMessage(ContainerRequestContext requestContext) {

        return messageSource.getMessage("jakarta.validation.constraints.NotBlank.message", new Object[0], null,
                requestContext.getAcceptableLanguages().stream().findFirst().orElseGet(Locale::getDefault));
    }

}
