package com.exemple.service.context;

import java.lang.reflect.Method;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import lombok.SneakyThrows;

public class UserContextExtension implements InvocationInterceptor {

    @Override
    public void interceptBeforeEachMethod(Invocation<@Nullable Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        this.interceptTestMethod(invocation, invocationContext, extensionContext);
    }

    @Override
    public void interceptTestMethod(Invocation<@Nullable Void> invocation,
            ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

        extensionContext.getTestMethod()
                .map(method -> method.getAnnotation(WithUserContext.class))
                .map(withContextUser -> new UserContext(withContextUser::name))
                .ifPresentOrElse(userContext -> {
                    ScopedValue.where(UserContext.USER_CONTEXT, userContext).run(() -> proceed(invocation));
                }, () -> proceed(invocation));
    }

    @SneakyThrows
    private static void proceed(Invocation<@Nullable Void> invocation) {
        invocation.proceed();
    }

}
