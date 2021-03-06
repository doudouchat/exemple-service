package com.exemple.service.context;

public final class ServiceContextExecution {

    private static ThreadLocal<ServiceContextExecution> executionContext = ThreadLocal.withInitial(ServiceContextExecution::new);

    private final ServiceContext model;

    private ServiceContextExecution() {

        this.model = new ServiceContext();
    }

    public static ServiceContext context() {

        return executionContext.get().model;
    }

    public static void destroy() {

        executionContext.remove();
    }

}
