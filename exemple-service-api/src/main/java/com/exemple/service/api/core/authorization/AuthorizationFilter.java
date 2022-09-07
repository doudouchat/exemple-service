package com.exemple.service.api.core.authorization;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.context.ServiceContextExecution;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;

@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Autowired
    private AuthorizationContextService service;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        ApiSecurityContext context = service.buildContext(requestContext.getHeaders());

        ServiceContextExecution.setPrincipal(context.getUserPrincipal());

        requestContext.setSecurityContext(context);

    }

}
