package com.exemple.service.resource.core.context;

import static com.exemple.service.resource.common.ResourceContext.KEYSPACE;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.exemple.service.application.common.exception.NotFoundApplicationException;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class ResourceContextAspect {

    private final ApplicationDetailService applicationDetailService;

    @Around("((@within(org.springframework.stereotype.Service) || @within(org.springframework.stereotype.Component)) "
            + "&& execution(public * com.exemple.service.resource..*.*(..))) "
            + "|| "
            + "execution(public * com.exemple.service.resource.common.validator.json.JsonValidator.*(..))")
    public Object initResourceContext(ProceedingJoinPoint joinPoint) {

        var app = ServiceContextExecution.context().getApp();

        Assert.notNull(app, "App is required");

        var keyspace = applicationDetailService.get(app).orElseThrow(() -> new NotFoundApplicationException(app)).getKeyspace();

        return ScopedValue.where(KEYSPACE, keyspace).call(() -> proceed(joinPoint));

    }

    @SneakyThrows
    private static Object proceed(ProceedingJoinPoint joinPoint) {
        return joinPoint.proceed();
    }

}
