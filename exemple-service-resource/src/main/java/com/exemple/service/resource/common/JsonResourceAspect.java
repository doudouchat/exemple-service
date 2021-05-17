package com.exemple.service.resource.common;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class JsonResourceAspect {

    @Around("@within(org.springframework.stereotype.Service) && execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource..*.*(..))")
    public Optional<JsonNode> returnOptionalJsonNode(ProceedingJoinPoint joinPoint) throws Throwable {

        @SuppressWarnings("unchecked")
        Optional<JsonNode> source = (Optional<JsonNode>) joinPoint.proceed();
        if (source.isPresent()) {
            return Optional.of(JsonNodeFilterUtils.clean(source.get()));
        }

        return source;
    }

}
