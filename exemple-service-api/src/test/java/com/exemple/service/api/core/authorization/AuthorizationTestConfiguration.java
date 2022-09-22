package com.exemple.service.api.core.authorization;

import java.security.PublicKey;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.api.core.authorization")
public class AuthorizationTestConfiguration {

    public static final RSAKey RSA_KEY;

    public static final RSAKey OTHER_RSA_KEY;

    public static final PublicKey PUBLIC_KEY;

    public static final PublicKey OTHER_PUBLIC_KEY;

    static {

        RSA_KEY = buildRSAKey();

        PUBLIC_KEY = toPublicKey(RSA_KEY);

        OTHER_RSA_KEY = buildRSAKey();

        OTHER_PUBLIC_KEY = toPublicKey(OTHER_RSA_KEY);

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

    }

    @Configuration
    @Profile("!AuthorizationMock")
    public class NotAuthorizationMock {

        @Value("${api.authorization.hazelcast.port}")
        private int port;

        @Bean
        public HazelcastInstance hazelcastInstanceServer() {

            Config config = new Config();
            config.getNetworkConfig().setPort(port);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);

            return Hazelcast.newHazelcastInstance(config);
        }

    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {

        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();

        YamlPropertiesFactoryBean properties = new YamlPropertiesFactoryBean();
        properties.setResources(new ClassPathResource("exemple-service-api-test.yml"));

        propertySourcesPlaceholderConfigurer.setProperties(properties.getObject());
        return propertySourcesPlaceholderConfigurer;
    }

    private static RSAKey buildRSAKey() {

        try {
            return new RSAKeyGenerator(2048).keyUse(KeyUse.SIGNATURE).generate();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PublicKey toPublicKey(RSAKey jwk) {

        try {
            return jwk.toPublicKey();
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
    }

}
