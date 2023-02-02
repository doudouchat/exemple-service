package com.exemple.service.resource.account;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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

    private final ApplicationDetailService applicationDetailService;

    private final AccountResource accountResource;

    @Around("@within(org.springframework.stereotype.Service)"
            + " && execution(public void com.exemple.service.customer.account.AccountResource.save(..))"
            + " && args(account)")
    public void checkUniquesProperties(ProceedingJoinPoint joinPoint, JsonNode account) {

        check(account, MAPPER.createObjectNode());

        save(joinPoint);

    }

    @Around("@within(org.springframework.stereotype.Service)"
            + " && execution(public void com.exemple.service.customer.account.AccountResource.save(..))"
            + " && args(account, previousAccount)")
    public void checkUniquesProperties(ProceedingJoinPoint joinPoint, JsonNode account, JsonNode previousAccount) {

        check(account, previousAccount);

        save(joinPoint);

    }

    @SneakyThrows
    private void save(ProceedingJoinPoint joinPoint) {
        joinPoint.proceed();
    }

    private void check(JsonNode account, JsonNode previousAccount) {

        this.applicationDetailService.get(ServiceContextExecution.context().getApp())
                .flatMap(applicationDetail -> Optional.ofNullable(applicationDetail.getAccount()))
                .ifPresent(a -> a.getUniqueProperties().forEach((String property) -> {

                    var actualEmail = account.path(property);
                    var previousEmail = previousAccount.path(property);

                    if (!actualEmail.isNull() && !actualEmail.equals(previousEmail)
                            && accountResource.getIdByUsername(property, actualEmail.textValue()).isPresent()) {
                        throw new UsernameAlreadyExistsException(actualEmail.textValue());
                    }
                }));

    }

}
