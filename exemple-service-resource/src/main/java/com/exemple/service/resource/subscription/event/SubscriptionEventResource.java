package com.exemple.service.resource.subscription.event;

import static com.exemple.service.context.UserContext.USER_CONTEXT;
import static com.exemple.service.resource.common.ResourceContext.KEYSPACE;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.ServiceContext;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.event.dao.SubscriptionEventDao;
import com.exemple.service.resource.subscription.event.mapper.SubscriptionEventMapper;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class SubscriptionEventResource {

    private final CqlSession session;

    private final ConcurrentMap<String, SubscriptionEventMapper> mappers = new ConcurrentHashMap<>();

    public BoundStatement saveEvent(JsonNode source, EventType eventType) {

        var email = source.get(SubscriptionField.EMAIL.field).stringValue();

        var context = ServiceContext.SERVICE_CONTEXT.get();

        var subscriptionEvent = new SubscriptionEvent();
        subscriptionEvent.setEmail(email);
        subscriptionEvent.setData(JsonNodeFilterUtils.clean(source));
        subscriptionEvent.setVersion(context.version());
        subscriptionEvent.setApplication(context.app());
        subscriptionEvent.setDate(context.date().toInstant());
        subscriptionEvent.setEventType(eventType);
        subscriptionEvent.setUser(USER_CONTEXT.get().principal().getName());

        return dao().create(subscriptionEvent);
    }

    public BoundStatement saveEvent(String email, EventType eventType) {

        var context = ServiceContext.SERVICE_CONTEXT.get();

        var subscriptionEvent = new SubscriptionEvent();
        subscriptionEvent.setEmail(email);
        subscriptionEvent.setVersion(context.version());
        subscriptionEvent.setApplication(context.app());
        subscriptionEvent.setDate(context.date().toInstant());
        subscriptionEvent.setEventType(eventType);
        subscriptionEvent.setUser(USER_CONTEXT.get().principal().getName());

        return dao().create(subscriptionEvent);
    }

    public SubscriptionEvent getByIdAndDate(String email, Instant date) {

        return dao().getByIdAndDate(email, date);

    }

    private SubscriptionEventDao dao() {

        return mappers.computeIfAbsent(KEYSPACE.get(), this::build).subscriptionEventDao();
    }

    private SubscriptionEventMapper build(String keyspace) {

        return SubscriptionEventMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
