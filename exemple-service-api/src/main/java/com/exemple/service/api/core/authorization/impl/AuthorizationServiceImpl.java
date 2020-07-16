package com.exemple.service.api.core.authorization.impl;

import java.util.logging.Level;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.exemple.service.api.core.authorization.AuthorizationService;

@Service
public class AuthorizationServiceImpl implements AuthorizationService {

    private final Client client;

    public AuthorizationServiceImpl(@Value("${api.authorization.connectionTimeout:3000}") int connectionTimeout,
            @Value("${api.authorization.socketTimeout:3000}") int socketTimeout) {

        client = ClientBuilder.newClient()

                // timeout

                .property(ClientProperties.CONNECT_TIMEOUT, connectionTimeout)

                .property(ClientProperties.READ_TIMEOUT, socketTimeout)

                // authentification

                .register(HttpAuthenticationFeature.basicBuilder().build())

                // logging

                .register(LoggingFeature.class)

                .property(LoggingFeature.LOGGING_FEATURE_VERBOSITY, LoggingFeature.Verbosity.PAYLOAD_ANY)

                .property(LoggingFeature.LOGGING_FEATURE_LOGGER_LEVEL, Level.FINE.getName());

    }

    @Override
    public Response tokenKey(String path, String username, String password) {

        return client.target(path + "/oauth/token_key").request()

                .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_USERNAME, username)
                .property(HttpAuthenticationFeature.HTTP_AUTHENTICATION_BASIC_PASSWORD, password).get();

    }

}
