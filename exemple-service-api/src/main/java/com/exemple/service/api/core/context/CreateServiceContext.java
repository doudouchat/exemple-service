package com.exemple.service.api.core.context;

import java.lang.reflect.Proxy;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.glassfish.jersey.server.ContainerRequest;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.model.SchemaBeanParam;
import com.exemple.service.context.ServiceContext;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Request;
import lombok.SneakyThrows;

@Aspect
@Component
@Priority(Priorities.USER)
public class CreateServiceContext implements ContainerRequestFilter {

    @Context
    private Request request;

    @Override
    public void filter(ContainerRequestContext requestContext) {

        // NOP
    }

    @Around("@annotation(jakarta.ws.rs.GET) || "
            + "@annotation(jakarta.ws.rs.HEAD) || "
            + "@annotation(jakarta.ws.rs.POST) || "
            + "@annotation(jakarta.ws.rs.PUT) || "
            + "@annotation(jakarta.ws.rs.PATCH)")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {

        return ScopedValue
                .where(ServiceContext.SERVICE_CONTEXT,
                        new ServiceContext(getHeader(ApplicationBeanParam.APP_HEADER), getHeader(SchemaBeanParam.VERSION_HEADER)))
                .call(() -> proceed(joinPoint));

    }

    @SneakyThrows
    private static Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

    private <T> T getHeader(String name) throws Throwable {

        return (T) Proxy.getInvocationHandler(request).invoke(request, ContainerRequest.class.getMethod("getHeaderString", String.class),
                new Object[] { name });
    }

}
