package com.exemple.service.api.core.authorization;

import java.security.Principal;
import java.util.Collections;
import java.util.regex.Pattern;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Payload;
import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationTokenValidation authorizationTokenValidation;

    static {

        BEARER = Pattern.compile("Bearer ");

    }

    public ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) throws AuthorizationException {

        String token = headers.getFirst("Authorization");

        if (token != null) {

            DecodedJWT jwt = JWT.decode(BEARER.matcher(token).replaceFirst(""));

            authorizationTokenValidation.checkSignature(jwt);
            authorizationTokenValidation.checkClientId(jwt, headers);
            authorizationTokenValidation.checkTokenIsInBlackList(jwt);

            return buildApiSecurityContext(jwt);

        }

        return buildApiSecurityAnonymousContext();
    }

    private static ApiSecurityContext buildApiSecurityContext(Payload payload) {

        Principal principal = () -> ObjectUtils.defaultIfNull(payload.getSubject(), getClientId(payload));
        return new ApiSecurityContext(principal, "https", payload.getClaim("scope").asList(String.class), payload.getClaim("profile").asString());
    }

    private static ApiSecurityContext buildApiSecurityAnonymousContext() {

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), null);
    }

    private static String getClientId(Payload payload) {

        return payload.getClaim("client_id").asString();
    }

}
