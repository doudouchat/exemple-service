package com.exemple.service.customer.account.resource

import com.exemple.service.context.ServiceContextExecution;

import groovy.transform.CompileDynamic

@CompileDynamic
class TestAccountServiceResource implements AccountServiceResource {

    private static final String CREATION_DATE = 'creation_date'

    private static final String UPDATE_DATE = 'update_date'

    @Override
    Map<String, Object> save(Map<String, Object> account) {
        account.put(CREATION_DATE, ServiceContextExecution.context().date.toString())
        account
    }

    @Override
    Map<String, Object> update(Map<String, Object> account) {
        account.put(UPDATE_DATE, ServiceContextExecution.context().date.toString())
        account
    }
}
