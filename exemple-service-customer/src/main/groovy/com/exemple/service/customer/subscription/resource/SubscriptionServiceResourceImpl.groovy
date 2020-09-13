package com.exemple.service.customer.subscription.resource

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.core.script.CustomiseResource

import groovy.transform.CompileDynamic

@CompileDynamic
class SubscriptionServiceResourceImpl implements CustomiseResource {

    private static final String SUBSCRIPTION_DATE = 'subscription_date'

    @Override
    Map<String, Object> create(Map<String, Object> source) {
        source.put(SUBSCRIPTION_DATE, ServiceContextExecution.context().date.toString())
        source
    }
}
