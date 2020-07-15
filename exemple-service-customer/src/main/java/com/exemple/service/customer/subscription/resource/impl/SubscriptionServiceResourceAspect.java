package com.exemple.service.customer.subscription.resource.impl;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.TransformUtils;
import com.exemple.service.customer.subscription.resource.SubscriptionServiceResource;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class SubscriptionServiceResourceAspect {

    private final SubscriptionServiceResource subscriptionServiceResource;

    public SubscriptionServiceResourceAspect(SubscriptionServiceResource subscriptionServiceResource) {
        this.subscriptionServiceResource = subscriptionServiceResource;
    }

    @Around("execution(public void com.exemple.service.resource.subscription.SubscriptionResource.save(*, *)) && args (email, source)")
    public void save(ProceedingJoinPoint joinPoint, String email, JsonNode source) throws Throwable {

        joinPoint.proceed(new Object[] { email, TransformUtils.transform(source, subscriptionServiceResource::save) });

    }

}
