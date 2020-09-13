package com.exemple.service.customer.account.resource;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.exemple.service.customer.core.script.CustomerScriptFactory;
import com.exemple.service.customer.core.script.CustomerScriptHelper;
import com.exemple.service.customer.core.script.CustomiseResource;
import com.exemple.service.customer.core.script.resource.CustomiseResourceImpl;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountCustomiseResource {

    private static final String BEAN_NAME = "accountServiceResource";

    private static ThreadLocal<JsonNode> resourceContext = new ThreadLocal<>();

    private final CustomiseResource customiseResource;

    public AccountCustomiseResource(CustomerScriptFactory customerScriptFactory) {

        this.customiseResource = new CustomiseResourceImpl(BEAN_NAME, customerScriptFactory);
    }

    public void setPreviousAccount(Optional<JsonNode> source) {
        source.ifPresent(resourceContext::set);
    }

    public JsonNode customiseAccount(JsonNode source) {
        try {
            return CustomerScriptHelper.execute(source, resourceContext.get(), this.customiseResource);
        } finally {
            resourceContext.remove();
        }
    }

}
