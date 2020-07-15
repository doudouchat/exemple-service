package com.exemple.service.customer.subscription.resource.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.core.script.CustomerScriptFactory;
import com.exemple.service.customer.subscription.resource.SubscriptionServiceResource;

@Component
public class SubscriptionServiceResourceImpl implements SubscriptionServiceResource {

    private static final String BEAN_NAME = "subscriptionServiceResource";

    private final CustomerScriptFactory scriptFactory;

    public SubscriptionServiceResourceImpl(CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
    }

    @Override
    public Map<String, Object> save(Map<String, Object> source) {
        return scriptFactory.getBean(BEAN_NAME, SubscriptionServiceResource.class).save(source);
    }

}
