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

        ServiceContext context = ServiceContextExecution.context();

        accountValidation.validate(source, context.getApp(), context.getVersion(), context.getProfile());

        UUID id = UUID.randomUUID();

        JsonNode account = accountResource.save(id, source);

        EventData eventData = new EventData(account, ACCOUNT, EventType.CREATE, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, context.getProfile(), account);
    }

    @Override
    public JsonNode save(UUID id, ArrayNode patch) throws AccountServiceException {

        ServiceContext context = ServiceContextExecution.context();

        JsonNode old = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        JsonNode source = JsonPatch.apply(patch, old);

        accountValidation.validate(source, old, context.getApp(), context.getVersion(), context.getProfile());

        JsonNode account = accountResource.save(id, source);

        EventData eventData = new EventData(account, ACCOUNT, EventType.UPDATE, context.getApp(), context.getVersion(), context.getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, context.getProfile(), account);
    }

    @Override
    public JsonNode get(UUID id) throws AccountServiceNotFoundException {

        ServiceContext context = ServiceContextExecution.context();

        JsonNode account = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        return schemaFilter.filter(context.getApp(), context.getVersion(), ACCOUNT, context.getProfile(), account);
    }
}
