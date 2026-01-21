package com.exemple.service.resource.account.history;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.AccountContextExecution;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.resource.account.history.dao.AccountHistoryDao;
import com.exemple.service.resource.account.history.mapper.AccountHistoryMapper;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.history.HistoryResource;
import com.exemple.service.resource.core.ResourceExecutionContext;

import tools.jackson.databind.JsonNode;

@Component
public class AccountHistoryResource {

    private final CqlSession session;

    private HistoryResource<UUID, AccountHistory> historyResource;

    private final ConcurrentMap<String, AccountHistoryMapper> mappers = new ConcurrentHashMap<>();

    public AccountHistoryResource(CqlSession session) {
        this.session = session;
        this.historyResource = new HistoryResource<>(this::dao, AccountHistory::new);

    }

    public List<AccountHistory> findById(UUID id) {

        return dao().findById(id).all();
    }

    public Collection<BoundStatement> saveHistories(JsonNode source) {

        var id = UUID.fromString(source.get(AccountField.ID.field).stringValue());

        return this.historyResource.saveHistories(id, source, AccountContextExecution.getPreviousAccount());
    }

    private AccountHistoryDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).accountHistoryDao();
    }

    private AccountHistoryMapper build(String keyspace) {

        return AccountHistoryMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
