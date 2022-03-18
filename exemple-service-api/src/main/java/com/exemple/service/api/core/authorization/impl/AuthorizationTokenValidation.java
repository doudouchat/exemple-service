package com.exemple.service.api.core.authorization.impl;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.api.core.authorization.AuthorizationException;
import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;

import lombok.RequiredArgsConstructor;

@Component
@Profile("!noSecurity")
@RequiredArgsConstructor
public class AuthorizationTokenValidation {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    private final AuthorizationTokenManager authorizationTokenManager;

    private final ApplicationDetailService applicationDetailService;

    private final AuthorizationAlgorithmFactory authorizationAlgorithmFactory;

    public void checkSignature(DecodedJWT jwt) throws AuthorizationException {

        JWTVerifier verifier = JWT.require(authorizationAlgorithmFactory.getAlgorithm()).build();
        try {
            verifier.verify(jwt.getToken());
        } catch (JWTVerificationException e) {
            throw new AuthorizationException(Response.Status.FORBIDDEN, e);
        }
    }

    public void checkClientId(DecodedJWT jwt, MultivaluedMap<String, String> headers) throws AuthorizationException {

        ApplicationDetail applicationDetail = applicationDetailService.get(headers.getFirst(ApplicationBeanParam.APP_HEADER));
        String clientId = getClientId(jwt);
        if (!applicationDetail.getClientIds().contains(clientId)) {
            throw new AuthorizationException(Response.Status.FORBIDDEN, clientId + " is forbidden");
        }
    }

    public void checkTokenIsInBlackList(DecodedJWT jwt) throws AuthorizationException {

        if (authorizationTokenManager.containsToken(jwt)) {
            throw new AuthorizationException(Response.Status.UNAUTHORIZED, jwt.getId() + " has been excluded");
        }
    }

    private static String getClientId(DecodedJWT jwt) {

        return jwt.getClaim("client_id").asString();
    }

}
