package com.exemple.service.api.core.authorization;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;

import com.exemple.service.api.common.security.ApiSecurityContext;

@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Autowired
    private AuthorizationContextService service;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        try {

            ApiSecurityContext context = service.buildContext(requestContext.getHeaders());

            requestContext.setSecurityContext(context);

        } catch (AuthorizationException e) {

            requestContext.abortWith(build(e));
        }

    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {

        if (requestContext.getSecurityContext() instanceof ApiSecurityContext) {

            service.cleanContext((ApiSecurityContext) requestContext.getSecurityContext(), responseContext.getStatusInfo());
        }

    }

    private static Response build(AuthorizationException e) {

        return Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build();
    }

}
