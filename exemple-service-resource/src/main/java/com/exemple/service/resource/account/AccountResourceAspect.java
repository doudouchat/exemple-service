package com.exemple.service.resource.account;

import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.exemple.service.application.common.model.ApplicationDetail;
import com.exemple.service.application.detail.ApplicationDetailService;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.exception.UsernameAlreadyExistsException;
import com.exemple.service.resource.account.username.AccountUsername;
import com.exemple.service.resource.account.username.AccountUsernameService;
import com.exemple.service.resource.common.lock.Lock;
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

    private final AccountUsernameService accountUsernameService;

    @Qualifier("accountCuratorFramework")
    private final CuratorFramework client;

    @Around(value = "@within(org.springframework.stereotype.Service)"
            + " && execution(public void com.exemple.service.customer.account.AccountResource.save(..))"
            + " && args(account)",
            argNames = "account")
    public void saveUniquesProperties(ProceedingJoinPoint joinPoint, JsonNode account) {

        this.saveUniquesProperties(joinPoint, account, MAPPER.createObjectNode());
    }

    @Around(value = "@within(org.springframework.stereotype.Service)"
            + " && execution(public void com.exemple.service.customer.account.AccountResource.save(..))"
            + " && args(account, previousAccount)",
            argNames = "account,previousAccount")
    public void saveUniquesProperties(ProceedingJoinPoint joinPoint, JsonNode account, JsonNode previousAccount) {

        var applicationDetail = this.applicationDetailService.get(ServiceContextExecution.context().getApp()).orElseThrow();

        var changedUsernames = accountUsernameService.findAllUsernames(applicationDetail, account, previousAccount).stream()
                .filter(AccountUsername::hasChanged)
                .toList();
        var lock = new AccountUsernameLock(changedUsernames, applicationDetail);
        lock.execute(() -> {

            accountUsernameService.findAllAlreadyExistsUsernames(changedUsernames).stream()
                    .findAny()
                    .ifPresent((AccountUsername username) -> {
                        throw new UsernameAlreadyExistsException(username.value());
                    });

            proceed(joinPoint);

            changedUsernames.forEach(accountUsernameService::saveUsername);
        });
    }

    @SneakyThrows
    private static void proceed(ProceedingJoinPoint joinPoint) {
        joinPoint.proceed();
    }

    private class AccountUsernameLock extends Lock {

        AccountUsernameLock(List<AccountUsername> usernames, ApplicationDetail applicationDetail) {

            super(usernames.stream()
                    .filter(AccountUsername::hasValue)
                    .map(username -> new InterProcessSemaphoreMutex(client, "/" + applicationDetail.getCompany() + "/username/" + username.value()))
                    .toList());
        }

    }
}
