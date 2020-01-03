package com.exemple.service.customer.account.impl;

import java.util.Optional;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.account.context.AccountContext;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class AccountContextAspect {

    @SuppressWarnings("unchecked")
    @Around("execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.account.AccountResource.get(*)) && args(id)")
    public Optional<JsonNode> get(ProceedingJoinPoint joinPoint, UUID id) throws Throwable {

        AccountContext context = AccountContext.get();

        if (context.getAccount(id) == null) {

            context.setAccount(id, ((Optional<JsonNode>) joinPoint.proceed()).orElse(null));
        }

        return Optional.ofNullable(context.getAccount(id));
    }

    @After("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource.account.AccountResource.update(*, *))")
    public void afterUpdate(JoinPoint joinPoint) {

        AccountContext.get().setAccount((UUID) joinPoint.getArgs()[0], null);
    }

}
