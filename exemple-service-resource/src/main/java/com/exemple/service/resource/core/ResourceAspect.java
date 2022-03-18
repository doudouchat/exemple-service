package com.exemple.service.resource.core;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.keystore.ResourceKeystore;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class ResourceAspect {

    private final ResourceKeystore resourceKeystore;

    @Before("@within(org.springframework.stereotype.Service) && execution(public * com.exemple.service.resource..*.*(..))")
    public void initResourceContext() {

        resourceKeystore.initKeyspaceResourceContext(ServiceContextExecution.context().getApp());

    }

}
