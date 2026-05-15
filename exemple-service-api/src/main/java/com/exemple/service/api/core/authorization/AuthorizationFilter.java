package com.exemple.service.api.core.authorization;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.context.UserContext;

import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import lombok.SneakyThrows;

@Aspect
@Component
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationFilter implements ContainerRequestFilter {

    private final AuthorizationContextService service;

    @Context
    private SecurityContext securityContext;

    @Inject
    public AuthorizationFilter(AuthorizationContextService service) {
        this.service = service;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {

        ApiSecurityContext context = service.buildContext(requestContext.getHeaders());

        requestContext.setSecurityContext(context);

    }

    @Around("@annotation(RolesAllowed)")
    public Object initResourceContext(ProceedingJoinPoint joinPoint, RolesAllowed rolesAllowed) {

        return ScopedValue.where(UserContext.USER_CONTEXT, new UserContext(securityContext.getUserPrincipal())).call(() -> proceed(joinPoint));

    }

    @SneakyThrows
    private static Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

}
