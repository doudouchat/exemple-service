package com.exemple.service.customer.account.resource.impl;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.account.resource.AccountServiceResource;
import com.exemple.service.customer.common.TransformUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class AccountServiceResourceAspect {

    private final AccountServiceResource accountServiceResource;

    public AccountServiceResourceAspect(AccountServiceResource accountServiceResource) {
        this.accountServiceResource = accountServiceResource;
    }

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource.account.AccountResource.save(*, *)) "
            + "&& args (id, source)")
    public JsonNode save(ProceedingJoinPoint joinPoint, UUID id, JsonNode source) throws Throwable {
        return (JsonNode) joinPoint.proceed(new Object[] { id, TransformUtils.transform(source, accountServiceResource::save) });
    }

    @Around("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource.account.AccountResource.update(*, *)) "
            + "&& args (id, source)")
    public JsonNode update(ProceedingJoinPoint joinPoint, UUID id, JsonNode source) throws Throwable {
        return (JsonNode) joinPoint.proceed(new Object[] { id, TransformUtils.transform(source, accountServiceResource::update) });
    }

}
