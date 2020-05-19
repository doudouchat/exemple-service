package com.exemple.service.schema.validation;

public final class SchemaValidationContext {

    private static ThreadLocal<SchemaValidationContext> executionContext = new ThreadLocal<>();

    private String app;

    private String version;

    private String resource;

    private String profile;

    private SchemaValidationContext() {

    }

    public static SchemaValidationContext get() {

        if (executionContext.get() == null) {
            executionContext.set(new SchemaValidationContext());
        }

        return executionContext.get();
    }

    public static void destroy() {

        executionContext.remove();
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

}
