package com.exemple.service.context;

import java.security.Principal;
import java.time.OffsetDateTime;

import com.exemple.service.context.ServiceContext.ServiceContextBuilder;

public final class ServiceContextExecution {

    private static ThreadLocal<ServiceContextExecution> executionContext = ThreadLocal.withInitial(ServiceContextExecution::new);

    private ServiceContext model;

    private ServiceContextExecution() {

        this.model = ServiceContext.builder()
                .date(OffsetDateTime.now())
                .principal(() -> "anonymous")
                .build();
    }

    private void reset(ServiceContext model) {
        this.model = model;
    }

    public static ServiceContext context() {

        return executionContext.get().model;
    }

    public static void setApp(String app) {

        ServiceContext model = builder().app(app).build();
        executionContext.get().reset(model);
    }

    public static void setDate(OffsetDateTime date) {

        ServiceContext model = builder().date(date).build();
        executionContext.get().reset(model);
    }

    public static void setPrincipal(Principal principal) {

        ServiceContext model = builder().principal(principal).build();
        executionContext.get().reset(model);
    }

    public static void setVersion(String version) {

        ServiceContext model = builder().version(version).build();
        executionContext.get().reset(model);
    }

    private static ServiceContextBuilder builder() {
        return executionContext.get().model.toBuilder();
    }

    public static void destroy() {

        executionContext.remove();
    }

}
