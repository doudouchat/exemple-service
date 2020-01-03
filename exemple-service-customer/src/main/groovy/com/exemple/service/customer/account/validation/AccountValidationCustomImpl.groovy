package com.exemple.service.customer.account.validation

import org.springframework.stereotype.Component

import groovy.transform.CompileDynamic

@Component
@CompileDynamic
class AccountValidationCustomImpl implements AccountValidationCustom {

    @Override
    void validate(Map<String, Object> form, Map<String, Object> old) {
    }

}
