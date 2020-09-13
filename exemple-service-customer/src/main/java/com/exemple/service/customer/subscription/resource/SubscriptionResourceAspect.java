package com.exemple.service.customer.subscription.resource;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class SubscriptionResourceAspect {

    private final SubscriptionCustomiseResource customiseResource;

    public SubscriptionResourceAspect(SubscriptionCustomiseResource customiseResource) {

        this.customiseResource = customiseResource;
    }

    @AfterReturning(value = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.subscription.SubscriptionResource.get(*))", returning = "source")
    public void get(Optional<JsonNode> source) {
        customiseResource.setPreviousSubscription(source);
    }

    @Around("execution(public void com.exemple.service.resource.subscription.SubscriptionResource.save(*, *)) && args (email, source)")
    public void save(ProceedingJoinPoint joinPoint, String email, JsonNode source) throws Throwable {

        joinPoint.proceed(new Object[] { email, customiseResource.customiseSubscription(source) });

    }

}
