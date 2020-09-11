package com.exemple.service.customer.account.resource;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class AccountResourceAspect {

    private final AccountCustomiseResource customiseResource;

    public AccountResourceAspect(AccountCustomiseResource customiseResource) {

        this.customiseResource = customiseResource;
    }

    @AfterReturning(value = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.account.AccountResource.get(*))", returning = "source")
    public void get(Optional<JsonNode> source) {
        customiseResource.setPreviousAccount(source);
    }

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource.account.AccountResource.save(*, *)) "
            + "&& args (id, source)")
    public JsonNode save(ProceedingJoinPoint joinPoint, UUID id, JsonNode source) throws Throwable {

        return (JsonNode) joinPoint.proceed(new Object[] { id, customiseResource.customiseAccount(source) });

    }

}
