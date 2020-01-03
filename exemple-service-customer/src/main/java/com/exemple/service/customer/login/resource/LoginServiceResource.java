package com.exemple.service.customer.login.resource;

import java.util.Map;

public interface LoginServiceResource {

    Map<String, Object> saveLogin(Map<String, Object> source);

    Map<String, Object> updateLogin(Map<String, Object> source);

}
