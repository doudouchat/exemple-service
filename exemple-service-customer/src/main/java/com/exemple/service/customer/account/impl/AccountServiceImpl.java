package com.exemple.service.customer.account.impl;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.exemple.service.customer.account.AccountService;
import com.exemple.service.customer.account.exception.AccountServiceException;
import com.exemple.service.customer.account.exception.AccountServiceNotFoundException;
import com.exemple.service.customer.account.validation.AccountValidation;
import com.exemple.service.event.model.EventData;
import com.exemple.service.event.model.EventType;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

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
    public JsonNode save(JsonNode source, String app, String version) throws AccountServiceException {

        accountValidation.validate(source, null, app, version);

        UUID id = UUID.randomUUID();

        JsonNode account = accountResource.save(id, source);

        EventData eventData = new EventData(account, ACCOUNT, EventType.CREATE, app, version, ResourceExecutionContext.get().getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return schemaFilter.filter(app, version, ACCOUNT, account);
    }

    @Override
    public JsonNode save(UUID id, JsonNode source, String app, String version) throws AccountServiceException {

        JsonNode old = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        accountValidation.validate(source, old, app, version);

        JsonNode account = accountResource.update(id, source);

        EventData eventData = new EventData(account, ACCOUNT, EventType.UPDATE, app, version, ResourceExecutionContext.get().getDate().toString());
        applicationEventPublisher.publishEvent(eventData);

        return schemaFilter.filter(app, version, ACCOUNT, account);
    }

    @Override
    public JsonNode get(UUID id, String app, String version) throws AccountServiceNotFoundException {

        JsonNode account = accountResource.get(id).orElseThrow(AccountServiceNotFoundException::new);

        return schemaFilter.filter(app, version, ACCOUNT, account);
    }
}
