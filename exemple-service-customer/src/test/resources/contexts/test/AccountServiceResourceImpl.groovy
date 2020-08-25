package com.exemple.service.customer.account.resource

import java.time.OffsetDateTime

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceResourceImpl implements AccountServiceResource {

    private static final String CREATION_DATE = 'creation_date'

    @Override
    Map<String, Object> save(Map<String, Object> account) {
        account.clear()
        account.put("TEST_KEY", "TEST_VALUE")
        account
    }
}
