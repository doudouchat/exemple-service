package com.exemple.service.api.core.authorization.impl;

import org.springframework.stereotype.Component;

import com.auth0.jwt.interfaces.Payload;
import com.hazelcast.core.HazelcastInstance;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationTokenManager {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    private final HazelcastInstance hazelcastInstance;

    public boolean containsToken(Payload payload) {

        return hazelcastInstance.getMap(TOKEN_BLACK_LIST).containsKey(payload.getId());
    }
}
