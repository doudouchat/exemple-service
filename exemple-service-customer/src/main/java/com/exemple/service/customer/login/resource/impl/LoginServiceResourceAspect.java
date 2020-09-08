package com.exemple.service.customer.login.resource.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.TransformUtils;
import com.exemple.service.customer.login.resource.LoginServiceResource;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class LoginServiceResourceAspect {

    private final LoginServiceResource loginServiceResource;

    public LoginServiceResourceAspect(LoginServiceResource loginServiceResource) {
        this.loginServiceResource = loginServiceResource;
    }

    @Around("execution(public void com.exemple.service.resource.login.LoginResource.save(*)) && args (source)")
    public void save(ProceedingJoinPoint joinPoint, JsonNode source) throws Throwable {

        joinPoint.proceed(new Object[] { TransformUtils.apply(source, loginServiceResource::saveLogin) });

    }

    @Around("execution(public void com.exemple.service.resource.login.LoginResource.update(*)) && args (source)")
    public void update(ProceedingJoinPoint joinPoint, JsonNode source) throws Throwable {

        joinPoint.proceed(new Object[] { TransformUtils.apply(source, loginServiceResource::saveLogin) });

    }

}
