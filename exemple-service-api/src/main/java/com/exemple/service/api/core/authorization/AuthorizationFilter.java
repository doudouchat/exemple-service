package com.exemple.service.api.core.authorization;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.context.ServiceContextExecution;

@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    @Autowired
    private AuthorizationContextService service;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        try {

            ApiSecurityContext context = service.buildContext(requestContext.getHeaders());

            ServiceContextExecution.setPrincipal(context.getUserPrincipal());

            requestContext.setSecurityContext(context);

        } catch (AuthorizationException e) {

            requestContext.abortWith(build(e));
        }

    }

    private static Response build(AuthorizationException e) {

        return Response.status(e.getStatus()).entity(e.getMessage()).build();
    }

}
