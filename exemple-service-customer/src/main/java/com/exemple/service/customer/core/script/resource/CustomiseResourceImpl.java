package com.exemple.service.customer.core.script.resource;

import java.util.Map;

import com.exemple.service.customer.core.script.CustomerScriptFactory;
import com.exemple.service.customer.core.script.CustomiseResource;

public class CustomiseResourceImpl implements CustomiseResource {

    private final String beanName;

    private final CustomerScriptFactory scriptFactory;

    public CustomiseResourceImpl(String beanName, CustomerScriptFactory scriptFactory) {
        this.scriptFactory = scriptFactory;
        this.beanName = beanName;
    }

    @Override
    public Map<String, Object> create(Map<String, Object> source) {
        return scriptFactory.getBean(beanName, CustomiseResource.class).create(source);
    }

    @Override
    public Map<String, Object> update(Map<String, Object> source, Map<String, Object> previousSource) {
        return scriptFactory.getBean(beanName, CustomiseResource.class).update(source, previousSource);
    }

}
