package com.exemple.service.resource.login.impl;

import java.util.Optional;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class LoginResourceAspect {

    @AfterReturning(pointcut = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.login.LoginResource.*(..))", returning = "source")
    public void afterReturning(Optional<JsonNode> source) {

        source.ifPresent(JsonNodeFilterUtils::clean);

    }

}
