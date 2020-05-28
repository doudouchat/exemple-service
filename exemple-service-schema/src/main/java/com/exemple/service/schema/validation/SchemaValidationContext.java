package com.exemple.service.schema.validation;

public final class SchemaValidationContext {

    private static ThreadLocal<SchemaValidationContext> executionContext = new ThreadLocal<>();

    private String resource;

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

}
