package com.exemple.service.customer.account.resource

import com.exemple.service.context.ServiceContextExecution;

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceResourceImpl implements AccountServiceResource {

    private static final String CREATION_DATE = 'creation_date'

    @Override
    Map<String, Object> save(Map<String, Object> account) {
        account.put(CREATION_DATE, ServiceContextExecution.context().date.toString())
        account
    }
}
