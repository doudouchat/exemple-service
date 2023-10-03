package com.exemple.service.api.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
public record ApiConfigurationProperties(Authorization authorization) {

    public static record Authorization(String jwkSetUri) {

    }

}
