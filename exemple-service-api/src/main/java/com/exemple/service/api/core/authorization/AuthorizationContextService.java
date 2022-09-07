package com.exemple.service.api.core.authorization;

import java.security.Principal;
import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import com.exemple.service.api.common.security.ApiSecurityContext;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;

import jakarta.ws.rs.core.MultivaluedMap;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthorizationContextService {

    private static final Pattern BEARER;

    private final AuthorizationTokenValidation authorizationTokenValidation;

    private final JwtDecoder jwtDecoder;

    static {

        BEARER = Pattern.compile("Bearer ");

    }

    public ApiSecurityContext buildContext(MultivaluedMap<String, String> headers) {

        String token = headers.getFirst("Authorization");

        if (token != null) {

            var jwt = jwtDecoder.decode(BEARER.matcher(token).replaceFirst(""));

            authorizationTokenValidation.checkClientId(jwt, headers);

            return buildApiSecurityContext(jwt);

        }

        return buildApiSecurityAnonymousContext();
    }

    private static ApiSecurityContext buildApiSecurityContext(Jwt jwt) {

        Principal principal = () -> ObjectUtils.defaultIfNull(jwt.getSubject(), getClientId(jwt));
        return new ApiSecurityContext(principal, "https", jwt.getClaimAsStringList("scope"), jwt.getClaimAsString("profile"));
    }

    private static ApiSecurityContext buildApiSecurityAnonymousContext() {

        return new ApiSecurityContext(() -> "anonymous", "http", Collections.emptyList(), null);
    }

    private static String getClientId(JwtClaimAccessor jwt) {

        return jwt.getClaimAsString("client_id");
    }

}
