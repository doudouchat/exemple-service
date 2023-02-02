package com.exemple.service.customer.login

import com.exemple.service.customer.account.AccountResource

import groovy.transform.CompileDynamic

@CompileDynamic
class LoginServiceImpl implements LoginService {

    AccountResource accountResource

    @Override
    Optional<UUID> get(String username) {

        Optional<UUID> id = accountResource.getIdByUsername('email', username)

        id
    }
}
