package com.exemple.service.api.common.script;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.customer.subscription.SubscriptionService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

@Component
public class SubscriptionServiceSupplier implements Supplier<SubscriptionService> {

    private static final String SUBSCRIPTION_BEAN = "subscriptionService";

    @Autowired
    private CustomerScriptFactory scriptFactory;

    @Context
    private ContainerRequestContext requestContext;

    @Override
    public SubscriptionService get() {
        var app = requestContext.getHeaderString(ApplicationBeanParam.APP_HEADER);
        return scriptFactory.getBean(SUBSCRIPTION_BEAN, SubscriptionService.class, app);

    }

}
