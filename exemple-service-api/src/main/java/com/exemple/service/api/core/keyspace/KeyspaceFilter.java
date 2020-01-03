package com.exemple.service.api.core.keyspace;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.resource.core.ResourceExecutionContext;

@Priority(Priorities.USER)
public class KeyspaceFilter implements ContainerRequestFilter {

    @Autowired
    private ApplicationDetailService applicationDetailService;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        if (requestContext.getHeaders().containsKey(ApplicationBeanParam.APP_HEADER)) {

            ApplicationDetail applicationDetail = applicationDetailService.get(requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER));

            ResourceExecutionContext.get().setKeyspace(applicationDetail.getKeyspace());
        }

    }

}
