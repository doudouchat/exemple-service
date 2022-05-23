package com.exemple.service.api.core.authorization;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenManager;
import com.exemple.service.api.core.authorization.impl.AuthorizationTokenValidation;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@ComponentScan(basePackages = "com.exemple.service.api.core.authorization")
@Profile("!noSecurity")
public class AuthorizationTestConfiguration {

    public static final Algorithm RSA256_ALGORITHM;

    public static final PublicKey PUBLIC_KEY;

    public static final Algorithm OTHER_RSA256_ALGORITHM;

    public static final PublicKey OTHER_PUBLIC_KEY;

    static {

        KeyPairGenerator keyPairGenerator = buildKeyPairGenerator();
        KeyPair keypair = keyPairGenerator.genKeyPair();
        PrivateKey privateKey = keypair.getPrivate();

        PUBLIC_KEY = keypair.getPublic();
        RSA256_ALGORITHM = Algorithm.RSA256((RSAPublicKey) PUBLIC_KEY, (RSAPrivateKey) privateKey);

        KeyPairGenerator otherKeyPairGenerator = buildKeyPairGenerator();
        KeyPair otherKeypair = otherKeyPairGenerator.genKeyPair();
        PrivateKey otherPrivateKey = otherKeypair.getPrivate();

        OTHER_PUBLIC_KEY = otherKeypair.getPublic();
        OTHER_RSA256_ALGORITHM = Algorithm.RSA256((RSAPublicKey) OTHER_PUBLIC_KEY, (RSAPrivateKey) otherPrivateKey);

    }

    public static KeyPairGenerator buildKeyPairGenerator() {

        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }

        keyPairGenerator.initialize(1024);
        return keyPairGenerator;
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

    public static class TestFilter implements ContainerRequestFilter {

        public SecurityContext context;

        @Override
        public void filter(ContainerRequestContext requestContext) {

            context = requestContext.getSecurityContext();

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

}
