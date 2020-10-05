package com.exemple.service.customer.account.resource

import com.exemple.service.customer.common.script.CustomiseResource

import groovy.transform.CompileDynamic

@CompileDynamic
class AccountServiceResourceImpl implements CustomiseResource {

    private static final String CREATION_DATE = 'creation_date'

    @Override
    Map<String, Object> create(Map<String, Object> source) {
        source.clear()
        source.put("TEST_KEY", "TEST_VALUE")
        source
    }
}
