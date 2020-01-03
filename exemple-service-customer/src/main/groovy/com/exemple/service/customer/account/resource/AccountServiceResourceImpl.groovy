package com.exemple.service.customer.account.resource

import java.time.OffsetDateTime

import org.springframework.stereotype.Component

import groovy.transform.CompileDynamic

@Component
@CompileDynamic
class AccountServiceResourceImpl implements AccountServiceResource {

    private static final String CREATION_DATE = 'creation_date'

    @Override
    Map<String, Object> save(UUID id, Map<String, Object> account) {
        account.put(CREATION_DATE, OffsetDateTime.now().toString())
        account
    }

    @Override
    Map<String, Object> saveOrUpdateAccount(UUID id, Map<String, Object> account) {
        account
    }

}
