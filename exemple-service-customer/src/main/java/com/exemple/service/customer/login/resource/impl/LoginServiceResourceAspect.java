package com.exemple.service.customer.login.resource.impl;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.exemple.service.customer.login.resource.LoginServiceResource;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Aspect
@Component
public class LoginServiceResourceAspect {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LoginServiceResource loginServiceResource;

    public LoginServiceResourceAspect(LoginServiceResource loginServiceResource) {
        this.loginServiceResource = loginServiceResource;
    }

    @Around("execution(public void com.exemple.service.resource.login.LoginResource.save(*)) && args (source)")
    public void saveLogin(ProceedingJoinPoint joinPoint, JsonNode source) throws Throwable {

        if (source != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
            source = JsonNodeUtils.create(loginServiceResource.saveLogin(sourceMap));
        } else {
            source = JsonNodeUtils.init();
        }
        joinPoint.proceed(new Object[] { source });
    }

    @Around("execution(public boolean com.exemple.service.resource.login.LoginResource.save(*, *)) " + "&& args (login,source)")
    public boolean updateLogin(ProceedingJoinPoint joinPoint, String login, JsonNode source) throws Throwable {

        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = MAPPER.convertValue(source, Map.class);
        source = JsonNodeUtils.create(loginServiceResource.updateLogin(sourceMap));
        return (boolean) joinPoint.proceed(new Object[] { login, source });
    }

}
