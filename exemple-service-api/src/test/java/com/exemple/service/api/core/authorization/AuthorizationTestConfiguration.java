package com.exemple.service.api.core.authorization;

import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.api.core.authorization")
public class AuthorizationTestConfiguration {

    public static final RSAKey RSA_KEY;

    public static final RSAKey OTHER_RSA_KEY;

    static {

        RSA_KEY = buildRSAKey();

        OTHER_RSA_KEY = buildRSAKey();

    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

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

        public NotAuthorizationMock(MockWebServer authorizationServer) throws JsonProcessingException {

            var jwkSet = new JWKSet(RSA_KEY).getKeys().stream().map(JWK::getRequiredParams).toList();

            var keys = Map.of("keys", jwkSet);

            authorizationServer.url("/oauth/jwks");
            authorizationServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBody(MAPPER.writeValueAsString(keys)));
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

        @Value("${api.authorization.port}")
        private int authorizationPort;

        @Bean(destroyMethod = "shutdown")
        public MockWebServer authorizationServer() throws IOException {
            MockWebServer authorizationServer = new MockWebServer();
            authorizationServer.start(authorizationPort);
            return authorizationServer;
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
