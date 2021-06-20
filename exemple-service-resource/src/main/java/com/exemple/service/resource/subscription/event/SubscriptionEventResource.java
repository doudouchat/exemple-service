package com.exemple.service.resource.subscription.event;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.ServiceContext;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.event.dao.SubscriptionEventDao;
import com.exemple.service.resource.subscription.event.mapper.SubscriptionEventMapper;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SubscriptionEventResource {

    private final CqlSession session;

    private final ConcurrentMap<String, SubscriptionEventMapper> mappers;

    public SubscriptionEventResource(CqlSession session) {
        this.session = session;
        this.mappers = new ConcurrentHashMap<>();

    }

    public BoundStatement saveEvent(JsonNode source, EventType eventType) {

        String email = source.get(SubscriptionField.EMAIL.field).textValue();

        ServiceContext context = ServiceContextExecution.context();

        SubscriptionEvent subscriptionEvent = new SubscriptionEvent();
        subscriptionEvent.setEmail(email);
        subscriptionEvent.setData(source);
        subscriptionEvent.setVersion(context.getVersion());
        subscriptionEvent.setApplication(context.getApp());
        subscriptionEvent.setDate(context.getDate().toInstant());
        subscriptionEvent.setEventType(eventType);

        return dao().create(subscriptionEvent);
    }

    public BoundStatement saveEvent(String email, EventType eventType) {

        ServiceContext context = ServiceContextExecution.context();

        SubscriptionEvent subscriptionEvent = new SubscriptionEvent();
        subscriptionEvent.setEmail(email);
        subscriptionEvent.setVersion(context.getVersion());
        subscriptionEvent.setApplication(context.getApp());
        subscriptionEvent.setDate(context.getDate().toInstant());
        subscriptionEvent.setEventType(eventType);

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
