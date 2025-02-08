package com.exemple.service.resource.subscription.event;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.event.dao.SubscriptionEventDao;
import com.exemple.service.resource.subscription.event.mapper.SubscriptionEventMapper;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SubscriptionEventResource {

    private final CqlSession session;

    private final ConcurrentMap<String, SubscriptionEventMapper> mappers = new ConcurrentHashMap<>();

    public BoundStatement saveEvent(JsonNode source, EventType eventType) {

        String email = source.get(SubscriptionField.EMAIL.field).textValue();

        var context = ServiceContextExecution.context();

        var subscriptionEvent = new SubscriptionEvent();
        subscriptionEvent.setEmail(email);
        subscriptionEvent.setData(JsonNodeFilterUtils.clean(source));
        subscriptionEvent.setVersion(context.getVersion());
        subscriptionEvent.setApplication(context.getApp());
        subscriptionEvent.setDate(context.getDate().toInstant());
        subscriptionEvent.setLocalDate(context.getDate().toLocalDate());
        subscriptionEvent.setEventType(eventType);
        subscriptionEvent.setUser(context.getPrincipal().getName());

        return dao().create(subscriptionEvent);
    }

    public BoundStatement saveEvent(String email, EventType eventType) {

        var context = ServiceContextExecution.context();

        var subscriptionEvent = new SubscriptionEvent();
        subscriptionEvent.setEmail(email);
        subscriptionEvent.setVersion(context.getVersion());
        subscriptionEvent.setApplication(context.getApp());
        subscriptionEvent.setDate(context.getDate().toInstant());
        subscriptionEvent.setLocalDate(context.getDate().toLocalDate());
        subscriptionEvent.setEventType(eventType);
        subscriptionEvent.setUser(context.getPrincipal().getName());

        return dao().create(subscriptionEvent);
    }

    public SubscriptionEvent getByIdAndDate(String email, Instant date) {

        return dao().getByIdAndDate(email, date);

    }

    private SubscriptionEventDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).subscriptionEventDao();
    }

    private SubscriptionEventMapper build(String keyspace) {

        return SubscriptionEventMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
