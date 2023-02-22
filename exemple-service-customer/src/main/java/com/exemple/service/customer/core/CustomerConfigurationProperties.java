package com.exemple.service.customer.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@ConfigurationProperties(prefix = "customer")
@RequiredArgsConstructor
@Getter
public class CustomerConfigurationProperties {

    private final Contexts contexts;

    @RequiredArgsConstructor
    @Getter
    public static class Contexts {

        private final String path;

    }

}
