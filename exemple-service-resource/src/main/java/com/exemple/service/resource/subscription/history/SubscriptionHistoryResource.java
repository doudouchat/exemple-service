package com.exemple.service.resource.subscription.history;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.SubscriptionContextExecution;
import com.exemple.service.resource.common.history.HistoryResource;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.history.dao.SubscriptionHistoryDao;
import com.exemple.service.resource.subscription.history.mapper.SubscriptionHistoryMapper;
import com.exemple.service.resource.subscription.model.SubscriptionHistory;
import com.fasterxml.jackson.databind.JsonNode;

@Component

public class SubscriptionHistoryResource {

    private final CqlSession session;

    private HistoryResource<String, SubscriptionHistory> historyResource;

    private final ConcurrentMap<String, SubscriptionHistoryMapper> mappers = new ConcurrentHashMap<>();

    public SubscriptionHistoryResource(CqlSession session) {
        this.session = session;
        this.historyResource = new HistoryResource<>(this::dao, SubscriptionHistory::new);

    }

    public List<SubscriptionHistory> findById(String id) {

        return dao().findById(id).all();
    }

    public Collection<BoundStatement> saveHistories(JsonNode source) {

        String email = source.get(SubscriptionField.EMAIL.field).textValue();

        return this.historyResource.saveHistories(email, source, SubscriptionContextExecution.getPreviousSubscription());
    }

    private SubscriptionHistoryDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).subscriptionHistoryDao();
    }

    private SubscriptionHistoryMapper build(String keyspace) {

        return SubscriptionHistoryMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
