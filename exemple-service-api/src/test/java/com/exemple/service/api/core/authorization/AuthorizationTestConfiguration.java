package com.exemple.service.api.core.authorization;

import java.text.ParseException;
import java.util.Map;

import org.mockito.Mockito;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.PlainJWT;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.api.core.authorization")
public class AuthorizationTestConfiguration {

    public static final RSAKey RSA_KEY;

    public static final RSAKey OTHER_RSA_KEY;

    static {

        RSA_KEY = buildRSAKey();

        OTHER_RSA_KEY = buildRSAKey();

    }

    @Configuration
    @Profile("AuthorizationMock")
    public class AuthorizationMock {

        @Bean
        public AuthorizationTokenManager authorizationTokenManager() {

            AuthorizationTokenManager authorizationTokenManager = Mockito.mock(AuthorizationTokenManager.class);
            Mockito.when(authorizationTokenManager.containsToken(Mockito.any())).thenReturn(false);

            return authorizationTokenManager;

        }

        @Bean
        public AuthorizationTokenValidation authorizationTokenValidation() {

            return Mockito.mock(AuthorizationTokenValidation.class);

        }

        @Bean
        public JwtDecoder decoder() {

            return (String token) -> {
                try {
                    PlainJWT jwt = PlainJWT.parse(token);
                    Map<String, Object> headers = jwt.getHeader().toJSONObject();
                    Map<String, Object> claims = jwt.getJWTClaimsSet().toJSONObject();
                    return Jwt
                            .withTokenValue(token)
                            .headers(h -> h.putAll(headers))
                            .claims(c -> c.putAll(claims))
                            .build();
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }

            };
        }

    }

    @Configuration
    @Import(AuthorizationServer.class)
    @Profile("!AuthorizationMock")
    public class NotAuthorizationMock {

        public NotAuthorizationMock(ClientAndServer authorizationClient) {

            var jwkSet = new JWKSet(RSA_KEY).getKeys().stream().map(JWK::getRequiredParams).toList();

            var keys = Map.of("keys", jwkSet);

            authorizationClient.when(HttpRequest.request()
                    .withMethod("GET")
                    .withPath("/oauth/jwks"))
                    .respond(HttpResponse.response()
                            .withBody(JsonBody.json(keys))
                            .withStatusCode(200));
        }

        @Bean
        public HazelcastInstance hazelcastInstanceServer(@Value("${api.authorization.hazelcast.port}") int port) {

            Config config = new Config();
            config.getNetworkConfig().setPort(port);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);

            return Hazelcast.newHazelcastInstance(config);
        }

    }

    @Configuration
    @Profile("!AuthorizationMock")
    public class AuthorizationServer {

        static {
            System.setProperty("mockserver.logLevel", "DEBUG");
        }

        @Value("${api.authorization.port}")
        private int authorizationPort;

        @Bean
        public ClientAndServer authorizationServer() {
            return ClientAndServer.startClientAndServer(authorizationPort);
        }

        @Bean
        @DependsOn("authorizationServer")
        public MockServerClient authorizationClient() {
            return new MockServerClient("localhost", authorizationPort);
        }

    }

    private static RSAKey buildRSAKey() {

        try {
            return new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

}
