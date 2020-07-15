package com.exemple.service.customer.subscription.resource

import com.exemple.service.context.ServiceContextExecution;

import groovy.transform.CompileDynamic

@CompileDynamic
class SubscriptionServiceResourceImpl implements SubscriptionServiceResource {

    private static final String SUBSCRIPTION_DATE = 'subscription_date'

    @Override
    Map<String, Object> save(Map<String, Object> subscription) {
        subscription.put(SUBSCRIPTION_DATE, ServiceContextExecution.context().date.toString())
        subscription
    }
}
