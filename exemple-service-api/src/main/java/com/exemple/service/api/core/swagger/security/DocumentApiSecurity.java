package com.exemple.service.api.core.swagger.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.exemple.service.api.core.swagger.DocumentApiResource;

import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentApiSecurity {

    @Value("${api.swagger.authorization.path:}")
    private final String path;

    public Map<String, SecurityScheme> buildSecurityScheme() {

        return Map.of(
                DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS, new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().clientCredentials(new OAuthFlow().tokenUrl(path + "/oauth/token"))),
                DocumentApiResource.OAUTH2_PASS, new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().password(new OAuthFlow().tokenUrl(path + "/oauth/token"))),
                DocumentApiResource.BEARER_AUTH, new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"));
    }

}
