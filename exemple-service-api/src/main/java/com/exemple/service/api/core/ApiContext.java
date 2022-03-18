package com.exemple.service.api.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Getter
public class ApiContext {

    @Value("${info.version:nc}")
    private final String version;

    @Value("${info.buildTime:nc}")
    private final String buildTime;

}
