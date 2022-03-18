package com.exemple.service.resource.core;

import org.springframework.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResourceExecutionContext {

    private static ThreadLocal<ResourceExecutionContext> executionContext = ThreadLocal.withInitial(ResourceExecutionContext::new);

    private String keyspace;

    public static ResourceExecutionContext get() {

        return executionContext.get();
    }

    public static void destroy() {

        executionContext.remove();
    }

    public String keyspace() {

        Assert.notNull(keyspace, "keyspace in ResourceExecutionContext must be required");

        return keyspace;
    }

    public boolean isKeyspaceNull() {

        return keyspace == null;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

}
