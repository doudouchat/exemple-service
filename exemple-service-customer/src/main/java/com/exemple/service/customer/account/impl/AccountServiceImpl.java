package com.exemple.service.customer.account.impl;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.customer.account.validation.AccountValidation;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.JsonPatch;

@Service
public class AccountServiceImpl implements AccountService {

    private static final String ACCOUNT = "account";

    private final AccountResource accountResource;

    private final AccountValidation accountValidation;

    private final SchemaFilter schemaFilter;

    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountServiceImpl(AccountResource accountResource, AccountValidation accountValidation, SchemaFilter schemaFilter,
            ApplicationEventPublisher applicationEventPublisher) {

        this.accountResource = accountResource;
        this.accountValidation = accountValidation;
        this.schemaFilter = schemaFilter;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public JsonNode save(JsonNode source) throws AccountServiceException {

        validate(source);

        JsonNode account = accountResource.save(UUID.randomUUID(), source);

        publish(account, EventType.CREATE);

        return filter(account);
    }

    @Override
    public JsonNode save(UUID id, ArrayNode patch) throws AccountServiceException {

        JsonNode old = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        JsonNode source = JsonPatch.apply(patch, old);

        validate(source, old);

        JsonNode account = accountResource.save(id, source);

        publish(account, EventType.UPDATE);

        return filter(source);
    }

    @Override
    public JsonNode get(UUID id) throws AccountServiceNotFoundException {

        JsonNode account = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        return filter(account);
    }

    private void validate(JsonNode account) {

        ServiceContext context = ServiceContextExecution.context();

        accountValidation.validate(account, context.getApp(), context.getVersion(), context.getProfile());
    }

    private void validate(JsonNode account, JsonNode previousAccount) {

        ServiceContext context = ServiceContextExecution.context();

        accountValidation.validate(account, previousAccount, context.getApp(), context.getVersion(), context.getProfile());
    }

    private void publish(JsonNode account, EventType type) {

        ServiceContext context = ServiceContextExecution.context();

        EventData eventData = new EventData(account, ACCOUNT, type, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);
    }

    private JsonNode filter(JsonNode account) {

        ServiceContext context = ServiceContextExecution.context();

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, context.getProfile(), account);
    }
}
