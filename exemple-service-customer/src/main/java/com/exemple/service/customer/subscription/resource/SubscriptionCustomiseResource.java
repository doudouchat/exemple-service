package com.exemple.service.customer.subscription.resource;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.core.script.CustomerScriptFactory;
import com.exemple.service.customer.core.script.CustomerScriptHelper;
import com.exemple.service.customer.core.script.CustomiseResource;
import com.exemple.service.customer.core.script.resource.CustomiseResourceImpl;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SubscriptionCustomiseResource {

    private static final String BEAN_NAME = "subscriptionServiceResource";

    private static ThreadLocal<JsonNode> resourceContext = new ThreadLocal<>();

    private final CustomiseResource customiseResource;

    public SubscriptionCustomiseResource(CustomerScriptFactory customerScriptFactory) {

        this.customiseResource = new CustomiseResourceImpl(BEAN_NAME, customerScriptFactory);
    }

    public void setPreviousSubscription(Optional<JsonNode> previousValue) {
        previousValue.ifPresent(resourceContext::set);
    }

    public JsonNode customiseSubscription(JsonNode source) {

        try {
            return CustomerScriptHelper.execute(source, resourceContext.get(), this.customiseResource);
        } finally {
            resourceContext.remove();
        }

    }

}
