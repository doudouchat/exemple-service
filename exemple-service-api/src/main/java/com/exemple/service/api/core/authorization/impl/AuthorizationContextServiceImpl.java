package com.exemple.service.api.core.authorization.impl;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.ws.rs.ForbiddenException;
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
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.statement.LoginStatement;
import com.exemple.service.resource.login.LoginResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.hazelcast.core.HazelcastInstance;

@Service
@Profile("!noSecurity")
public class AuthorizationContextServiceImpl implements AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    private final LoginResource loginResource;

    private final HazelcastInstance hazelcastInstance;

    private final ApplicationDetailService applicationDetailService;

    @Value("${api.resourceId}")
    private String resourceId;

    public AuthorizationContextServiceImpl(AuthorizationAlgorithmFactory authorizationAlgorithmFactory, LoginResource loginResource,
            HazelcastInstance hazelcastInstance, ApplicationDetailService applicationDetailService) {

        this.authorizationAlgorithmFactory = authorizationAlgorithmFactory;
        this.loginResource = loginResource;
        this.hazelcastInstance = hazelcastInstance;
        this.applicationDetailService = applicationDetailService;

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

            return new ApiSecurityContext(principal, "https", payload.getClaim("scope").asList(String.class), payload);

        }

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), null);
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

    @Override
    public void verifyAccountId(UUID id, ApiSecurityContext securityContext) {

        JsonNode login = loginResource.get(securityContext.getUserPrincipal().getName()).orElseGet(JsonNodeUtils::init).get(LoginStatement.ID);

        if (!id.toString().equals(login.asText(null))) {

            throw new ForbiddenException();
        }

    }

    @Override
    public void verifyLogin(String login, ApiSecurityContext securityContext) {

        if (!login.equals(securityContext.getUserPrincipal().getName())) {

            throw new ForbiddenException();
        }

    }

}
