package com.exemple.service.resource.schema;

import javax.validation.constraints.NotNull;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.core.keystore.ResourceKeystore;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@Order(0)
@RequiredArgsConstructor
public class SchemaResourceAspect {

    private final ResourceKeystore resourceKeystore;

    @Before("@within(org.springframework.stereotype.Service) && execution(public * com.exemple.service.resource.schema..*.*(*,..)) && args(app, ..))")
    public void initResourceContext(@NotNull String app) {

        resourceKeystore.initKeyspaceResourceContext(app);

    }

}
