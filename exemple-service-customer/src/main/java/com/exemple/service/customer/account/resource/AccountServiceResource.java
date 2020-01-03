package com.exemple.service.customer.account.resource;

import java.util.Map;
import java.util.UUID;

public interface AccountServiceResource {

    Map<String, Object> save(UUID id, Map<String, Object> account);

    Map<String, Object> saveOrUpdateAccount(UUID id, Map<String, Object> account);

}
