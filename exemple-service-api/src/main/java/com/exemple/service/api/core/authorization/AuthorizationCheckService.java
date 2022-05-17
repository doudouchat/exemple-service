package com.exemple.service.api.core.authorization;

import java.util.UUID;

public interface AuthorizationCheckService {

    default void verifyAccountId(UUID id) {

    }

    default void verifyLogin(String login) {

    }

}
