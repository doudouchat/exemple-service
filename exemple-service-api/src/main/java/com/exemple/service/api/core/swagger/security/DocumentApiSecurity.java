package com.exemple.service.api.core.swagger.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.exemple.service.api.core.swagger.DocumentApiResource;

import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Service
@Profile("!noSecurity")
public class DocumentApiSecurity {

    private final String path;

    public DocumentApiSecurity(@Value("${api.swagger.authorization.path:}") String path) {

        this.path = path;
    }

    public Map<String, SecurityScheme> buildSecurityScheme() {

        Map<String, SecurityScheme> securitySchemes = new HashMap<>();

        SecurityScheme oauth2ClientCredentials = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().clientCredentials(new OAuthFlow().tokenUrl(path + "/oauth/token")));
        securitySchemes.put(DocumentApiResource.OAUTH2_CLIENT_CREDENTIALS, oauth2ClientCredentials);

        SecurityScheme oauth2Password = new SecurityScheme().type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows().password(new OAuthFlow().tokenUrl(path + "/oauth/token")));
        securitySchemes.put(DocumentApiResource.OAUTH2_PASS, oauth2Password);

        SecurityScheme bearerAuth = new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
        securitySchemes.put(DocumentApiResource.BEARER_AUTH, bearerAuth);

        return securitySchemes;
    }

}
