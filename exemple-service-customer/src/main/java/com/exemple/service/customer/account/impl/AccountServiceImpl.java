package com.exemple.service.customer.account.impl;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.customer.core.script.CustomiseResourceHelper;
import com.exemple.service.customer.core.script.CustomiseValidationHelper;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.account.AccountResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT = "account";

    private final AccountResource accountResource;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final CustomiseResourceHelper customiseResourceHelper;

    private final CustomiseValidationHelper customiseValidationHelper;

    public AccountServiceImpl(AccountResource accountResource, ApplicationEventPublisher applicationEventPublisher,
            CustomiseResourceHelper customiseResourceHelper, CustomiseValidationHelper customiseValidationHelper) {

        this.accountResource = accountResource;
        this.applicationEventPublisher = applicationEventPublisher;
        this.customiseResourceHelper = customiseResourceHelper;
        this.customiseValidationHelper = customiseValidationHelper;
    }

    @Override
    public JsonNode save(JsonNode source) {

        customiseValidationHelper.validate(ACCOUNT, source);

        JsonNode account = customiseResourceHelper.customise(ACCOUNT, source);

        accountResource.save(account);

        publish(account, EventType.CREATE);

        return account;
    }

    @Override
    public JsonNode save(JsonNode source, JsonNode previousSource) {

        customiseValidationHelper.validate(ACCOUNT, source, previousSource);

        JsonNode account = customiseResourceHelper.customise(ACCOUNT, source, previousSource);

        accountResource.save(account, previousSource);

        publish(account, EventType.UPDATE);

        return account;
    }

    @Override
    public JsonNode get(UUID id) throws AccountServiceNotFoundException {

        return accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);
    }

    private void publish(JsonNode account, EventType type) {

        ServiceContext context = ServiceContextExecution.context();

        EventData eventData = new EventData(account, ACCOUNT, type, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);
    }
}
