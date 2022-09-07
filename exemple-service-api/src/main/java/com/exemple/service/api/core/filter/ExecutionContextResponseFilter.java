package com.exemple.service.api.core.filter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.ResourceExecutionContext;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

@Priority(Priorities.USER)
public class ExecutionContextResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {

        ServiceContextExecution.setDate(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        ServiceContextExecution.setApp(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER));
        ServiceContextExecution.setVersion(requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER));

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        ServiceContextExecution.destroy();
        ResourceExecutionContext.destroy();

    }

}
