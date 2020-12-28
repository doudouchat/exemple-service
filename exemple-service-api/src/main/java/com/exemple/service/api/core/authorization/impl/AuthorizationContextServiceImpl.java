package com.exemple.service.api.core.authorization.impl;

import java.security.Principal;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.AuthorizationContextService;
import com.exemple.service.api.core.authorization.AuthorizationException;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;

@Service
@Profile("!noSecurity")
public class AuthorizationContextServiceImpl implements AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    private final AuthorizationTokenManager authorizationTokenManager;

    private final ApplicationDetailService applicationDetailService;

    private final String resourceId;

    public AuthorizationContextServiceImpl(AuthorizationAlgorithmFactory authorizationAlgorithmFactory,
            AuthorizationTokenManager authorizationTokenManager, ApplicationDetailService applicationDetailService,
            @Value("${api.resourceId}") String resourceId) {

        this.authorizationAlgorithmFactory = authorizationAlgorithmFactory;
        this.authorizationTokenManager = authorizationTokenManager;
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

            Payload payload = buildPayload(token);

            checkClientId(payload, headers);
            checkTokenIsInBlackList(payload);

            return buildApiSecurityContext(payload);

        }

        return buildApiSecurityAnonymousContext();
    }

    @Override
    public void cleanContext(ApiSecurityContext securityContext, Response.StatusType statusInfo) {

        if (isSuccess(statusInfo) && isSingleUse(securityContext)) {

            authorizationTokenManager.addTokenInBlackList(securityContext.getPayload());
        }

    }

    private Payload buildPayload(String token) throws AuthorizationException {

        JWTVerifier verifier = JWT.require(authorizationAlgorithmFactory.getAlgorithm()).withAudience(resourceId).build();
        try {
            return verifier.verify(BEARER.matcher(token).replaceFirst(""));
        } catch (JWTVerificationException e) {
            throw new AuthorizationException(e);
        }
    }

    private void checkClientId(Payload payload, MultivaluedMap<String, String> headers) throws AuthorizationException {

        ApplicationDetail applicationDetail = applicationDetailService.get(headers.getFirst(ApplicationBeanParam.APP_HEADER));
        String clientId = getClientId(payload);
        if (!applicationDetail.getClientIds().contains(clientId)) {
            throw new AuthorizationException(Response.Status.FORBIDDEN, clientId + " is forbidden");
        }
    }

    private void checkTokenIsInBlackList(Payload payload) throws AuthorizationException {

        if (authorizationTokenManager.containsToken(payload)) {
            throw new AuthorizationException(Response.Status.UNAUTHORIZED, payload.getId() + " has been excluded");
        }
    }

    private static ApiSecurityContext buildApiSecurityContext(Payload payload) {

        Principal principal = () -> ObjectUtils.defaultIfNull(payload.getSubject(), getClientId(payload));
        return new ApiSecurityContext(principal, "https", payload.getClaim("scope").asList(String.class), payload.getClaim("profile").asString(),
                payload);
    }

    private static ApiSecurityContext buildApiSecurityAnonymousContext() {

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), null, null);
    }

    private static boolean isSuccess(Response.StatusType statusInfo) {

        return Response.Status.Family.SUCCESSFUL == ObjectUtils.defaultIfNull(statusInfo.getFamily(), Response.Status.Family.OTHER);
    }

    private static boolean isSingleUse(ApiSecurityContext securityContext) {

        return securityContext.getPayload() != null
                && Boolean.TRUE.equals(securityContext.getPayload().getClaims().getOrDefault("singleUse", new NullClaim()).asBoolean());
    }

    private static String getClientId(Payload payload) {

        return payload.getClaim("client_id").asString();
    }

}
