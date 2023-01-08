package com.exemple.service.api.core.authorization.impl;

import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Component;

import com.hazelcast.core.HazelcastInstance;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationTokenManager {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    private final HazelcastInstance hazelcastInstance;

    public boolean containsToken(JwtClaimAccessor jwt) {

        return jwt.getId() != null && hazelcastInstance.getMap(TOKEN_BLACK_LIST).containsKey(jwt.getId());
    }
}
