package com.exemple.service.customer.login.resource

import org.mindrot.jbcrypt.BCrypt

import com.exemple.service.customer.common.script.CustomiseResource

import groovy.transform.CompileDynamic

@CompileDynamic
class LoginServiceResourceImpl implements CustomiseResource {

    private static final String PASSWORD = 'password'

    private static final String ROLES = 'roles'

    @Override
    Map<String, Object> create(Map<String, Object> source) {
        if (source.containsKey(PASSWORD) && !((String) source.get(PASSWORD)).startsWith('{bcrypt}')) {
            encryptPassword(source);
        }
        source.put(ROLES, ['ROLE_ACCOUNT'])
        source
    }

    @Override
    Map<String, Object> update(Map<String, Object> source, Map<String, Object> previousSource) {
        if (source.containsKey(PASSWORD) && !((String) source.get(PASSWORD)).startsWith('{bcrypt}')) {
            encryptPassword(source);
        }
        source
    }

    private void encryptPassword(Map<String, Object> source) {
        source.put(PASSWORD, '{bcrypt}' + BCrypt.hashpw(source.get(PASSWORD), BCrypt.gensalt()))
    }
}
