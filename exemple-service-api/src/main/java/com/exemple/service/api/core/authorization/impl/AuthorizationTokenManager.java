package com.exemple.service.api.core.authorization.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.auth0.jwt.interfaces.Payload;
import com.hazelcast.core.HazelcastInstance;

@Component
@Profile("!noSecurity")
public class AuthorizationTokenManager {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    private final HazelcastInstance hazelcastInstance;

    public AuthorizationTokenManager(HazelcastInstance hazelcastInstance) {

        this.hazelcastInstance = hazelcastInstance;
    }

    public boolean containsToken(Payload payload) {

        return hazelcastInstance.getMap(TOKEN_BLACK_LIST).containsKey(payload.getId());
    }
}
