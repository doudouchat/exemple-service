package com.exemple.service.api.core.authorization;

import java.util.UUID;

import com.exemple.service.api.common.security.ApiSecurityContext;

public interface AuthorizationCheckService {

    default void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

    }

    default void verifyLogin(String login, ApiSecurityContext securityContext) {

    }

}
