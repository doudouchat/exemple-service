package com.exemple.service.resource.core;

import java.security.Principal;
import java.time.OffsetDateTime;

import org.springframework.util.Assert;

public final class ResourceExecutionContext {

    private static ThreadLocal<ResourceExecutionContext> executionContext = new ThreadLocal<>();

    private OffsetDateTime date = OffsetDateTime.now();

    private String keyspace;

    private Principal principal = () -> "anonymous";

    private String application;

    private String version;

    private ResourceExecutionContext() {

    }

    public static ResourceExecutionContext get() {

        if (executionContext.get() == null) {
            executionContext.set(new ResourceExecutionContext());
        }

        return executionContext.get();
    }

    public static void destroy() {

        executionContext.remove();
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }

    public String keyspace() {

        Assert.notNull(keyspace, "keyspace in ResourceExecutionContext must be required");

        return keyspace;
    }

    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
