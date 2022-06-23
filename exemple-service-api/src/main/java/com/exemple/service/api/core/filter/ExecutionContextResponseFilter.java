package com.exemple.service.api.core.filter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.ResourceExecutionContext;

@Priority(Priorities.USER)
public class ExecutionContextResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {

        ServiceContextExecution.context().setDate(OffsetDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        ServiceContextExecution.context().setApp(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER));
        ServiceContextExecution.context().setVersion(requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER));

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        ServiceContextExecution.destroy();
        ResourceExecutionContext.destroy();

    }

}
