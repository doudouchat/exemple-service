package com.exemple.service.api.login.context;

import java.util.Optional;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

@Aspect
@Component
public class LoginContextAspect {

    private final LoginContext context;

    public LoginContextAspect(LoginContext context) {
        this.context = context;
    }

    @SuppressWarnings("unchecked")
    @Around("execution(public java.util.Optional<com.fasterxml.jackson.databind.JsonNode> "
            + "com.exemple.service.resource.login.LoginResource.get(*)) && args(username)")
    public Optional<JsonNode> get(ProceedingJoinPoint joinPoint, String username) throws Throwable {

        if (context.getLogin(username) == null) {

            context.setLogin(username, ((Optional<JsonNode>) joinPoint.proceed()).orElse(null));
        }

        return Optional.ofNullable(context.getLogin(username));
    }

    @After("execution(public boolean com.exemple.service.resource.login.LoginResource.save(*, *))")
    public void afterUpdate(JoinPoint joinPoint) {

        context.setLogin((String) joinPoint.getArgs()[0], null);
    }

    @After("execution(public void com.exemple.service.resource.login.LoginResource.delete(*))")
    public void afterDelete(JoinPoint joinPoint) {

        context.setLogin((String) joinPoint.getArgs()[0], null);
    }

}
