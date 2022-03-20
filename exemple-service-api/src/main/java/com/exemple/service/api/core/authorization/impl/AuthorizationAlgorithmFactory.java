package com.exemple.service.api.core.authorization.impl;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.auth0.jwt.algorithms.Algorithm;
import com.exemple.service.api.core.authorization.AuthorizationException;

import lombok.SneakyThrows;

@Component
@Profile("!noSecurity")
public class AuthorizationAlgorithmFactory {

    private static final Pattern RSA_PUBLIC_KEY;

    private final AuthorizationClient authorizationClient;

    private final KeyFactory keyFactory;

    private final ConcurrentMap<String, Algorithm> algorithms;

    private final String defaultPath;

    private final String clientId;

    private final String clientSecret;

    static {

        RSA_PUBLIC_KEY = Pattern.compile("-----BEGIN PUBLIC KEY-----(.*)-----END PUBLIC KEY-----", Pattern.DOTALL);
    }

    public AuthorizationAlgorithmFactory(AuthorizationClient authorizationClient, @Value("${api.authorization.path}") String defaultPath,
            @Value("${api.authorization.client.clientId}") String clientId, @Value("${api.authorization.client.clientSecret}") String clientSecret)
            throws NoSuchAlgorithmException {

        this.authorizationClient = authorizationClient;
        this.keyFactory = KeyFactory.getInstance("RSA");
        this.algorithms = new ConcurrentHashMap<>();
        this.defaultPath = defaultPath;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public Algorithm getAlgorithm() {

        return algorithms.computeIfAbsent(defaultPath, this::buildAlgorithm);
    }

    public void resetAlgorithm() {
        algorithms.compute(defaultPath, (String path, Algorithm algorithm) -> null);
    }

    @SneakyThrows(AuthorizationException.class)
    private Algorithm buildAlgorithm(String path) {

        Response response = this.authorizationClient.tokenKey(path, clientId, clientSecret);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {

            throw new AuthorizationException("HTTP GET token_key failed",
                    new ClientErrorException(response.readEntity(String.class), response.getStatus()));
        }

        Map<String, String> body = response.readEntity(new GenericType<Map<String, String>>() {
        });

        Matcher publicKeyMatcher = RSA_PUBLIC_KEY.matcher(body.get("value"));

        Assert.isTrue(publicKeyMatcher.lookingAt(), "Pattern is invalid");

        final byte[] content = Base64.decodeBase64(publicKeyMatcher.group(1).getBytes(StandardCharsets.UTF_8));

        KeySpec keySpec = new X509EncodedKeySpec(content);
        PublicKey publicKey;
        try {
            publicKey = this.keyFactory.generatePublic(keySpec);
        } catch (InvalidKeySpecException e) {
            throw new AuthorizationException(e);
        }

        return Algorithm.RSA256((RSAPublicKey) publicKey, null);
    }

}
