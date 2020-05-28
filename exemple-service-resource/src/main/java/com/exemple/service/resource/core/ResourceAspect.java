package com.exemple.service.resource.core;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.core.keystore.ResourceKeystore;

@Aspect
@Component
public class ResourceAspect {

    private final ResourceKeystore resourceKeystore;

    public ResourceAspect(ResourceKeystore resourceKeystore) {
        this.resourceKeystore = resourceKeystore;
    }

    @Before("@within(org.springframework.stereotype.Service) && execution(public * com.exemple.service.resource..*.*(..))")
    public void initResourceContext() {

        resourceKeystore.initKeyspaceResourceContext(ServiceContextExecution.context().getApp());

    }

}
