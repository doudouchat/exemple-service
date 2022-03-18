package com.exemple.service.resource.account.event;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.resource.account.event.dao.AccountEventDao;
import com.exemple.service.resource.account.event.mapper.AccountEventMapper;
import com.exemple.service.resource.account.model.AccountEvent;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountEventResource {

    private final CqlSession session;

    private final ConcurrentMap<String, AccountEventMapper> mappers = new ConcurrentHashMap<>();

    public BoundStatement saveEvent(JsonNode source, EventType eventType) {

        UUID id = UUID.fromString(source.get(AccountField.ID.field).textValue());

        ServiceContext context = ServiceContextExecution.context();

        AccountEvent accountEvent = new AccountEvent();
        accountEvent.setId(id);
        accountEvent.setData(source);
        accountEvent.setVersion(context.getVersion());
        accountEvent.setApplication(context.getApp());
        accountEvent.setDate(context.getDate().toInstant());
        accountEvent.setLocalDate(context.getDate().toLocalDate());
        accountEvent.setEventType(eventType);

        return dao().create(accountEvent);
    }

    public AccountEvent getByIdAndDate(UUID id, Instant date) {

        return dao().getByIdAndDate(id, date);

    }

    private AccountEventDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).accountEventDao();
    }

    private AccountEventMapper build(String keyspace) {

        return AccountEventMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
