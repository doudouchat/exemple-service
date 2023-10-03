package com.exemple.service.api.core.authorization;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import com.exemple.service.api.core.ApiConfigurationProperties;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AuthorizationConfiguration {

    private final AuthorizationTokenManager authorizationTokenManager;

    @Bean
    public JwtDecoder decoder(ApiConfigurationProperties apiProperties) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(apiProperties.authorization().jwkSetUri()).build();

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(Duration.ZERO),
                new TokenNotExcludedValidator()));

        return jwtDecoder;
    }

    private class TokenNotExcludedValidator implements OAuth2TokenValidator<Jwt> {

        @Override
        public OAuth2TokenValidatorResult validate(Jwt jwt) {
            if (authorizationTokenManager.containsToken(jwt)) {
                return OAuth2TokenValidatorResult.failure(new OAuth2Error("custom_code", jwt.getId() + " has been excluded", null));
            }
            return OAuth2TokenValidatorResult.success();
        }
    }

}
