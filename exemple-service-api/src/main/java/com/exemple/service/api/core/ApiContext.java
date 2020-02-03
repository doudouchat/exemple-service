package com.exemple.service.api.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiContext {

    @Value("${info.version:nc}")
    private String version;

    @Value("${info.buildTime:nc}")
    private String buildTime;

    public String getVersion() {
        return version;
    }

    public String getBuildTime() {
        return buildTime;
    }

}
