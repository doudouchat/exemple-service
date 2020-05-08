package com.exemple.service.resource.account.impl;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

@Aspect
@Component
public class AccountResourceAspect {

    @Before("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource.account.AccountResource.save(*, *)) "
            + "&& args(id, source)")
    public void beforeSave(UUID id, JsonNode source) {

        JsonNodeFilterUtils.clean(source);
    }

    @Before("execution(public com.fasterxml.jackson.databind.JsonNode com.exemple.service.resource.account.AccountResource.update(*, *)) "
            + "&& args(id, source)")
    public void beforeUpdate(UUID id, JsonNode source) {

        filter(source);
    }

    private static void filter(JsonNode source) {

        JsonNodeFilterUtils.filter(source, (Map.Entry<String, JsonNode> e) -> {

            if (e.getValue().isArray()) {

                ((ObjectNode) source).replace(e.getKey(),
                        JsonNodeUtils.create(Streams.stream(e.getValue().elements()).filter(node -> !node.isNull()).collect(Collectors.toList())));

                filter(source.get(e.getKey()));
            }
        });
    }

    @AfterReturning(pointcut = "execution(public com.fasterxml.jackson.databind.JsonNode "
            + "com.exemple.service.resource.account.AccountResource.*(..))", returning = "source")
    public void afterReturning(JsonNode source) {

        JsonNodeFilterUtils.clean(source);

    }

    @AfterReturning(pointcut = "execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.account.AccountResource.*(..))", returning = "source")
    public void afterReturning(Optional<JsonNode> source) {

        source.ifPresent(JsonNodeFilterUtils::clean);

    }

}
