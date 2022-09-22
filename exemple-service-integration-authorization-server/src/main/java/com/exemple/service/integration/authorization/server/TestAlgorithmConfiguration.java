package com.exemple.service.integration.authorization.server;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import com.nimbusds.jose.crypto.RSASSASigner;

import lombok.Getter;

@Configuration
public class TestAlgorithmConfiguration {

    @Getter
    private final byte[] publicKeyContent;

    private final byte[] privateKeyContent;

    public TestAlgorithmConfiguration(@Value("${public-key-location:classpath:public_key}") Resource publicKeyResource,
            @Value("${private-key-location:classpath:private_key}") Resource privateKeyResource) throws IOException {

        this.publicKeyContent = IOUtils.toByteArray(publicKeyResource.getInputStream());
        this.privateKeyContent = IOUtils.toByteArray(privateKeyResource.getInputStream());

    }

    @Bean
    public RSASSASigner signer() throws GeneralSecurityException {
        return new RSASSASigner(privateKey());
    }

    @Bean
    public RSAPublicKey publicKey() throws GeneralSecurityException {

        var key = new String(publicKeyContent, StandardCharsets.UTF_8);

        String publicKeyPEM = key.replace("-----BEGIN PUBLIC KEY-----", "").replaceAll(System.lineSeparator(), "").replace("-----END PUBLIC KEY-----",
                "");

        byte[] encoded = Base64.decodeBase64(publicKeyPEM);

        var keyFactory = KeyFactory.getInstance("RSA");
        var keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    @Bean
    public RSAPrivateKey privateKey() throws GeneralSecurityException {

        var key = new String(privateKeyContent, StandardCharsets.UTF_8);

        String privateKeyPEM = key.replace("-----BEGIN PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        var keyFactory = KeyFactory.getInstance("RSA");
        var keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }
}
