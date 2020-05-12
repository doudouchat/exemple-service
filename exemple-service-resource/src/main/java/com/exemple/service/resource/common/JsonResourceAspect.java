package com.exemple.service.resource.common;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class JsonResourceAspect {

    @AfterReturning(pointcut = "@within(org.springframework.stereotype.Service) "
            + "&& execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource..*.*(..))", returning = "source")
    public void afterReturning(JsonNode source) {

        JsonNodeFilterUtils.clean(source);

    }

    @AfterReturning(pointcut = "@within(org.springframework.stereotype.Service) "
            + "&& execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource..*.*(..))", returning = "source")
    public void afterReturning(Optional<JsonNode> source) {

        source.ifPresent(JsonNodeFilterUtils::clean);

    }

    @AfterReturning(pointcut = "@within(org.springframework.stereotype.Service) "
            + "&& execution(public java.util.List<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource..*.*(..))", returning = "source")
    public void afterReturning(List<JsonNode> source) {

        source.forEach(JsonNodeFilterUtils::clean);

    }

}
