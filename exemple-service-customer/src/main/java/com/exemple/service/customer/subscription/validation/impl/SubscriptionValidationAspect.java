package com.exemple.service.customer.subscription.validation.impl;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.common.TransformUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class SubscriptionValidationAspect {

    private final SubscriptionValidationCustomImpl subscriptionValidationCustom;

    public SubscriptionValidationAspect(SubscriptionValidationCustomImpl subscriptionValidationCustom) {
        this.subscriptionValidationCustom = subscriptionValidationCustom;
    }

    @After("execution(public void com.exemple.service.customer.subscription.validation.SubscriptionValidation.validate(*, ..)) "
            + "&& args(form, ..)")
    public void validate(JsonNode form) {

        TransformUtils.transform(form, null, subscriptionValidationCustom::validate);

    }

}
