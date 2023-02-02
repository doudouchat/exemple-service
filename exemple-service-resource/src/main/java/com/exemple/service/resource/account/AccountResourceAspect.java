package com.exemple.service.resource.account;

import java.util.ArrayList;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountResource;
import com.exemple.service.resource.account.exception.UsernameAlreadyExistsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Aspect
@Component
@RequiredArgsConstructor
public class AccountResourceAspect {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Value("${resource.zookeeper.ttlMs.product:30000}")
    private final long productTtlMs;

    private final ApplicationDetailService applicationDetailService;

    private final AccountResource accountResource;

    @Qualifier("accountCuratorFramework")
    private final CuratorFramework client;

    @Around("@within(org.springframework.stereotype.Service)"
            + " && execution(public void com.exemple.service.customer.account.AccountResource.save(..))"
            + " && args(account)")
    public void checkUniquesProperties(ProceedingJoinPoint joinPoint, JsonNode account) {

        save(joinPoint, account, MAPPER.createObjectNode());

    }

    @Around("@within(org.springframework.stereotype.Service)"
            + " && execution(public void com.exemple.service.customer.account.AccountResource.save(..))"
            + " && args(account, previousAccount)")
    public void checkUniquesProperties(ProceedingJoinPoint joinPoint, JsonNode account, JsonNode previousAccount) {

        save(joinPoint, account, previousAccount);

    }

    @SneakyThrows
    private void save(ProceedingJoinPoint joinPoint, JsonNode account, JsonNode previousAccount) {

        var usernameFields = new ArrayList<UsernameField>();
        var locks = new ArrayList<InterProcessLock>();

        this.applicationDetailService.get(ServiceContextExecution.context().getApp())
                .ifPresent(
                        applicationDetail -> applicationDetail.getAccount().getUniqueProperties().stream()
                                .filter((String property) -> {
                                    var actualEmail = account.path(property);
                                    var previousEmail = previousAccount.path(property);
                                    return !actualEmail.isNull() && !actualEmail.equals(previousEmail);
                                })
                                .map((String property) -> new UsernameField(property, account.path(property).textValue()))
                                .forEach((UsernameField usernameField) -> {
                                    usernameFields.add(usernameField);
                                    locks.add(new InterProcessSemaphoreMutex(client,
                                            "/" + applicationDetail.getCompany() + "/username/" + usernameField.field));
                                }));

        try {
            locks.forEach(AccountResourceAspect::acquire);

            usernameFields.forEach(this::checkUsername);

            joinPoint.proceed();

        } finally {
            locks.forEach(AccountResourceAspect::release);
        }

    }

    private void checkUsername(UsernameField usernameField) {

        if (accountResource.getIdByUsername(usernameField.field, usernameField.value).isPresent()) {

            throw new UsernameAlreadyExistsException(usernameField.value);

        }
    }

    @SneakyThrows
    private static void acquire(InterProcessLock lock) {
        lock.acquire();
    }

    @SneakyThrows
    private static void release(InterProcessLock lock) {
        lock.release();
    }

    private static record UsernameField(String field, String value) {

    }

}
