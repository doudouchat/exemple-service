package com.exemple.service.customer.login.resource;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class LoginResourceAspect {

    private final LoginCustomiseResource customiseResource;

    public LoginResourceAspect(LoginCustomiseResource customiseResource) {

        this.customiseResource = customiseResource;
    }

    @AfterReturning(value = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.login.LoginResource.get(*))", returning = "source")
    public void setPreviousAccount(Optional<JsonNode> source) {
        customiseResource.setPreviousLogin(source);
    }

    @Around("execution(public void com.exemple.service.resource.login.LoginResource.save(*)) && args (source)")
    public void save(ProceedingJoinPoint joinPoint, JsonNode source) throws Throwable {

        joinPoint.proceed(new Object[] { customiseResource.customiseLogin(source) });

    }

    @Around("execution(public void com.exemple.service.resource.login.LoginResource.update(*)) && args (source)")
    public void update(ProceedingJoinPoint joinPoint, JsonNode source) throws Throwable {

        joinPoint.proceed(new Object[] { customiseResource.customiseLogin(source) });

    }

}
