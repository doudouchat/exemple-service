package com.exemple.service.api.core.authorization.impl;

import javax.ws.rs.core.MultivaluedMap;

import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Component;

import com.exemple.service.api.common.model.ApplicationBeanParam;
import com.exemple.service.application.detail.ApplicationDetailService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuthorizationTokenValidation {

    public static final String TOKEN_BLACK_LIST = "token.black_list";

    private final ApplicationDetailService applicationDetailService;

    public void checkClientId(JwtClaimAccessor jwt, MultivaluedMap<String, String> headers) {

        String application = headers.getFirst(ApplicationBeanParam.APP_HEADER);
        var applicationDetail = applicationDetailService.get(application).orElseThrow(() -> new BadJwtException(application + " is forbidden"));
        String clientId = getClientId(jwt);
        if (!applicationDetail.getClientIds().contains(clientId)) {
            throw new BadJwtException(clientId + " is forbidden");
        }
    }

    private static String getClientId(JwtClaimAccessor jwt) {

        return jwt.getClaimAsString("client_id");
    }

}
