package com.exemple.service.context;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import lombok.SneakyThrows;

public class ServiceContextExtension implements InvocationInterceptor {

    @Override
    public void interceptBeforeAllMethod(Invocation<@Nullable Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        var withServiceContext = invocationContext.getExecutable().getAnnotation(WithServiceContext.class);

        if (withServiceContext != null) {
            ServiceContext serviceContext = new ServiceContext(withServiceContext.app(), withServiceContext.version());
            ScopedValue.where(ServiceContext.SERVICE_CONTEXT, serviceContext).run(() -> proceed(invocation));
        } else {
            proceed(invocation);
        }
    }

    @Override
    public void interceptBeforeEachMethod(Invocation<@Nullable Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        this.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(Invocation<@Nullable Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        this.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<@Nullable Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        extensionContext.getTestMethod()
                .map(method -> method.getAnnotation(WithServiceContext.class))
                .map(withServiceContext -> new ServiceContext(withServiceContext.app(), withServiceContext.version()))
                .ifPresentOrElse(serviceContext -> {
                    ScopedValue.where(ServiceContext.SERVICE_CONTEXT, serviceContext).run(() -> proceed(invocation));
                }, () -> proceed(invocation));
    }

    @SneakyThrows
    private static void proceed(Invocation<@Nullable Void> invocation) {
        invocation.proceed();
    }

}
