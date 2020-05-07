package com.exemple.service.customer.login.resource

import org.mindrot.jbcrypt.BCrypt
import org.springframework.stereotype.Component

import groovy.transform.CompileDynamic

@Component
@CompileDynamic
class LoginServiceResourceImpl implements LoginServiceResource {

    private static final String PASSWORD = 'password'

    private static final String ROLES = 'roles'

    @Override
    Map<String, Object> saveLogin(Map<String, Object> source) {
        Map<String, ?> login = updateLogin(source)
        login.put(ROLES, ['ROLE_ACCOUNT'])
        login
    }

    @Override
    Map<String, Object> updateLogin(Map<String, Object> source) {
        if (source && source.containsKey(PASSWORD)) {
            source.put(PASSWORD, '{bcrypt}' + BCrypt.hashpw(source.get(PASSWORD), BCrypt.gensalt()))
        }
        source
    }

}
