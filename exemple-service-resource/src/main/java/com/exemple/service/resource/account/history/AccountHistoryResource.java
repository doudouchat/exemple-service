package com.exemple.service.resource.account.history;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
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
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.flipkart.zjsonpatch.DiffFlags;
import com.flipkart.zjsonpatch.JsonDiff;
import com.google.common.collect.Streams;

@Component
public class AccountHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountHistoryResource.class);

    private static final JsonNode DEFAULT_HISTORY_VALUE = new ObjectMapper().nullNode();

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

    public Collection<BoundStatement> saveHistories(final UUID id, JsonNode source, JsonNode previousSource, OffsetDateTime now) {

        return this.createHistories(id, source, previousSource, now);
    }

    private Collection<BoundStatement> createHistories(final UUID id, JsonNode source, JsonNode previousSource, OffsetDateTime now) {

        Map<String, AccountHistory> histories = findById(id).stream().collect(Collectors.toMap(AccountHistory::getField, Function.identity()));

        AccountHistory defaultHistory = new AccountHistory();
        defaultHistory.setDate(now.toInstant().minusNanos(1));
        defaultHistory.setValue(DEFAULT_HISTORY_VALUE);

        PreparedStatement prepared = session.prepare("INSERT INTO " + ResourceExecutionContext.get().keyspace()
                + ".account_history (id,date,field,value,previous_value,application,version,user) VALUES (?,?,?,?,?,?,?,?)");

        ArrayNode patch = (ArrayNode) JsonDiff.asJson(JsonNodeFilterUtils.clean(previousSource), JsonNodeFilterUtils.clean(source),
                EnumSet.of(DiffFlags.OMIT_COPY_OPERATION, DiffFlags.OMIT_MOVE_OPERATION, DiffFlags.OMIT_VALUE_ON_REMOVE));

        return Streams.stream(patch.elements())

                .map((JsonNode element) -> {
                    String path = element.get("path").asText();
                    JsonNode value = element.path("value");

                    AccountHistory history = new AccountHistory();
                    history.setId(id);
                    history.setField(path);
                    history.setDate(now.toInstant());
                    history.setValue(value);
                    history.setApplication(ServiceContextExecution.context().getApp());
                    history.setVersion(ServiceContextExecution.context().getVersion());
                    history.setUser(ServiceContextExecution.context().getPrincipal().getName());
                    history.setPreviousValue(histories.getOrDefault(path, defaultHistory).getValue());

                    return history;

                })

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
