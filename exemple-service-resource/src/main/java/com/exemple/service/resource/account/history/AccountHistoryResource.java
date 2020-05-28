package com.exemple.service.resource.account.history;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.history.dao.AccountHistoryDao;
import com.exemple.service.resource.account.history.mapper.AccountHistoryMapper;
import com.exemple.service.resource.account.impl.AccountResourceImpl;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.MetadataSchemaUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountHistoryResource.class);

    private final CqlSession session;

    private final ConcurrentMap<String, AccountHistoryMapper> mappers;

    public AccountHistoryResource(CqlSession session) {
        this.session = session;
        this.mappers = new ConcurrentHashMap<>();

    }

    public List<AccountHistory> findById(UUID id) {

        return dao().findById(id).all();
    }

    public AccountHistory findByIdAndField(UUID id, String field) {

        return dao().findByIdAndField(id, field);
    }

    public Collection<BoundStatement> createHistories(final UUID id, JsonNode source, OffsetDateTime now) {

        Map<String, AccountHistory> histories = findById(id).stream().collect(Collectors.toMap(AccountHistory::getField, Function.identity()));

        AccountHistory defaultHistory = new AccountHistory();
        defaultHistory.setDate(now.toInstant().minusNanos(1));

        PreparedStatement prepared = session.prepare("INSERT INTO " + ResourceExecutionContext.get().keyspace()
                + ".account_history (id,date,field,value,previous_value,application,version,user) VALUES (?,?,?,?,?,?,?,?)");

        return MetadataSchemaUtils.transformColumnToLine(session, AccountResourceImpl.ACCOUNT_TABLE, source).stream()

                .filter(line -> now.toInstant().isAfter(histories.getOrDefault(line.getKey(), defaultHistory).getDate()))

                .map((Map.Entry<String, JsonNode> line) -> {

                    JsonNodeFilterUtils.clean(line.getValue());

                    AccountHistory history = new AccountHistory();
                    history.setId(id);
                    history.setField(line.getKey());
                    history.setDate(now.toInstant());
                    history.setValue(line.getValue());
                    history.setApplication(ServiceContextExecution.context().getApp());
                    history.setVersion(ServiceContextExecution.context().getVersion());
                    history.setUser(ServiceContextExecution.context().getPrincipal().getName());
                    history.setPreviousValue(histories.getOrDefault(line.getKey(), defaultHistory).getValue());

                    return history;
                })

                .filter((AccountHistory history) -> !Objects.equals(history.getValue(),
                        histories.getOrDefault(history.getField(), defaultHistory).getValue()))

                .map((AccountHistory history) -> {
                    LOG.debug("save history account {} {} {}", history.getId(), history.getField(), history.getValue());
                    return prepared.bind(history.getId(), history.getDate(), history.getField(), history.getValue(), history.getPreviousValue(),
                            history.getApplication(), history.getVersion(), history.getUser());
                }).collect(Collectors.toList());
    }

    private AccountHistoryDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).accountHistoryDao();
    }

    private AccountHistoryMapper build(String keyspace) {

        return AccountHistoryMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
