package com.exemple.service.context;

public final class ServiceContextExecution {

    private static ThreadLocal<ServiceContextExecution> executionContext = new ThreadLocal<>();

    private final ServiceContext model;

    private ServiceContextExecution() {

        this.model = new ServiceContext();
    }

    public static ServiceContext context() {

        if (executionContext.get() == null) {
            executionContext.set(new ServiceContextExecution());
        }

        return executionContext.get().model;
    }

    public static void destroy() {

        executionContext.remove();
    }

}
