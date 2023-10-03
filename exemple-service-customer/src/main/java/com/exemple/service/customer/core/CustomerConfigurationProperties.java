package com.exemple.service.customer.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customer")
public record CustomerConfigurationProperties(Contexts contexts) {

    public static record Contexts(String path) {

    }

}
