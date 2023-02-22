package com.exemple.service.api.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "api")
@RequiredArgsConstructor
@Getter
public class ApiConfigurationProperties {

    private final Authorization authorization;

    @RequiredArgsConstructor
    @Getter
    public static class Authorization {

        private final String jwkSetUri;

    }

}
