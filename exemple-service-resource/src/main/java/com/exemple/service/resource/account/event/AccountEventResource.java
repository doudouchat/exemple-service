package com.exemple.service.resource.account.event;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.resource.account.event.dao.AccountEventDao;
import com.exemple.service.resource.account.event.mapper.AccountEventMapper;
import com.exemple.service.resource.account.model.AccountEvent;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class AccountEventResource {

    private final CqlSession session;

    private final ConcurrentMap<String, AccountEventMapper> mappers = new ConcurrentHashMap<>();

    public BoundStatement saveEvent(JsonNode source, EventType eventType) {

        var id = UUID.fromString(source.get(AccountField.ID.field).stringValue());

        var context = ServiceContextExecution.context();

        var accountEvent = new AccountEvent();
        accountEvent.setId(id);
        accountEvent.setData(JsonNodeFilterUtils.clean(source));
        accountEvent.setVersion(context.getVersion());
        accountEvent.setApplication(context.getApp());
        accountEvent.setDate(context.getDate().toInstant());
        accountEvent.setEventType(eventType);
        accountEvent.setUser(context.getPrincipal().getName());

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
