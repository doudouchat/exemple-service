package com.exemple.service.api.core.filter;

import java.time.OffsetDateTime;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;

@Priority(Priorities.USER)
public class ExecutionContextResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        ResourceExecutionContext.get().setDate(OffsetDateTime.now());

        if (requestContext.getHeaders().containsKey(ApplicationBeanParam.APP_HEADER)) {

            ApplicationDetail applicationDetail = applicationDetailService.get(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER));

            ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());
            ResourceExecutionContext.get().setApplication(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER));
        }

        if (requestContext.getHeaders().containsKey(SchemaBeanParam.VERSION_HEADER)) {

            ResourceExecutionContext.get().setVersion(requestContext.getHeaderString(SchemaBeanParam.VERSION_HEADER));
        }

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        ResourceExecutionContext.destroy();

    }

}
