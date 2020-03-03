package com.exemple.service.api.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiContext {

    private final String version;

    private final String buildTime;

    public ApiContext(@Value("${info.version:nc}") String version, @Value("${info.buildTime:nc}") String buildTime) {

        this.version = version;
        this.buildTime = buildTime;
    }

    public String getVersion() {
        return version;
    }

    public String getBuildTime() {
        return buildTime;
    }

}
