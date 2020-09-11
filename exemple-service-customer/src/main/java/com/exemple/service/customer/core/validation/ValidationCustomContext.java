package com.exemple.service.customer.core.validation;

public final class ValidationCustomContext {

    private static ThreadLocal<ValidationCustomContext> executionContext = ThreadLocal.withInitial(ValidationCustomContext::new);

    private String resource;

    private ValidationCustomContext() {

    }

    public static ValidationCustomContext context() {

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
