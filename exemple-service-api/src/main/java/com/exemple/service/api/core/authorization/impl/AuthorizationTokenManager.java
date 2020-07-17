package com.exemple.service.api.core.authorization.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.service.api.core.authorization.AuthorizationConfiguration;
import com.hazelcast.core.HazelcastInstance;

@Component
@Profile("!noSecurity")
public class AuthorizationTokenManager {

    private final HazelcastInstance hazelcastInstance;

    public AuthorizationTokenManager(HazelcastInstance hazelcastInstance) {

        this.hazelcastInstance = hazelcastInstance;
    }

    public boolean containsToken(Payload payload) {

        return payload.getId() != null && hazelcastInstance.getMap(AuthorizationConfiguration.TOKEN_BLACK_LIST).containsKey(payload.getId());
    }

    public void addTokenInBlackList(Payload payload) {

        Assert.notNull(payload.getId(), PublicClaims.JWT_ID + " is required in accessToken");

        Assert.notNull(payload.getExpiresAt(), PublicClaims.EXPIRES_AT + " is required in accessToken");

        hazelcastInstance.getMap(AuthorizationConfiguration.TOKEN_BLACK_LIST)
                .put(payload.getId(), payload.getExpiresAt(),
                        ChronoUnit.SECONDS.between(LocalDateTime.now(),
                                Instant.ofEpochSecond(payload.getExpiresAt().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()),
                        TimeUnit.SECONDS);
    }
}
