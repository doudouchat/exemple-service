package com.exemple.service.api.core.authorization.impl;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.impl.PublicClaims;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationConfiguration;
import com.exemple.service.api.core.authorization.AuthorizationContextService;
import com.exemple.service.api.core.authorization.AuthorizationException;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.hazelcast.core.HazelcastInstance;

@Service
@Profile("!noSecurity")
public class AuthorizationContextServiceImpl implements AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    private final HazelcastInstance hazelcastInstance;

    private final ApplicationDetailService applicationDetailService;

    private final String resourceId;

    public AuthorizationContextServiceImpl(AuthorizationAlgorithmFactory authorizationAlgorithmFactory, HazelcastInstance hazelcastInstance,
            ApplicationDetailService applicationDetailService, @Value("${api.resourceId}") String resourceId) {

        this.authorizationAlgorithmFactory = authorizationAlgorithmFactory;
        this.hazelcastInstance = hazelcastInstance;
        this.applicationDetailService = applicationDetailService;
        this.resourceId = resourceId;
    }

    static {

        BEARER = Pattern.compile("Bearer ");

    }

    @Override
    public ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException {

        String token = headers.getFirst("Authorization");

        if (token != null) {

            JWTVerifier verifier = JWT.require(authorizationAlgorithmFactory.getAlgorithm()).withAudience(resourceId).build();

            Payload payload;
            try {
                payload = verifier.verify(BEARER.matcher(token).replaceFirst(""));
            } catch (JWTVerificationException e) {
                throw new AuthorizationException(e);
            }

            ApplicationDetail applicationDetail = applicationDetailService.get(headers.getFirst(ApplicationBeanParam.APP_HEADER));
            String clientId = payload.getClaim("client_id").asString();
            if (!applicationDetail.getClientIds().contains(clientId)) {
                throw new AuthorizationException(clientId + " is forbidden");
            }

            if (payload.getId() != null && hazelcastInstance.getMap(AuthorizationConfiguration.TOKEN_BLACK_LIST).containsKey(payload.getId())) {
                throw new AuthorizationException(payload.getId() + " has been excluded");
            }

            Principal principal = () -> ObjectUtils.defaultIfNull(payload.getSubject(), clientId);

            return new ApiSecurityContext(principal, "https", payload.getClaim("scope").asList(String.class), payload.getClaim("profile").asString(),
                    payload);

        }

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), null, null);
    }

    @Override
    public void cleanContext(ApiSecurityContext securityContext, Response.StatusType statusInfo) {

        if (Response.Status.Family.SUCCESSFUL == ObjectUtils.defaultIfNull(statusInfo.getFamily(), Response.Status.Family.OTHER)
                && securityContext.getPayload() != null
                && Boolean.TRUE.equals(securityContext.getPayload().getClaims().getOrDefault("singleUse", new NullClaim()).asBoolean())) {

            Assert.notNull(securityContext.getPayload().getId(), PublicClaims.JWT_ID + " is required in accessToken");

            Assert.notNull(securityContext.getPayload().getExpiresAt(), PublicClaims.EXPIRES_AT + " is required in accessToken");

            hazelcastInstance.getMap(AuthorizationConfiguration.TOKEN_BLACK_LIST).put(securityContext.getPayload().getId(),
                    securityContext.getPayload().getExpiresAt(), ChronoUnit.SECONDS.between(LocalDateTime.now(), Instant
                            .ofEpochSecond(securityContext.getPayload().getExpiresAt().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime()),
                    TimeUnit.SECONDS);
        }

    }

}
