package com.exemple.service.resource.account.history;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.exemple.service.resource.account.history.dao.AccountHistoryDao;
import com.exemple.service.resource.account.history.mapper.AccountHistoryMapper;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.util.StringHelper;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.parameter.ParameterResource;
import com.exemple.service.resource.parameter.model.ParameterEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Streams;

@Component
public class AccountHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountHistoryResource.class);

    private final CqlSession session;

    private final ParameterResource parameterResource;

    private final ConcurrentMap<String, AccountHistoryMapper> mappers;

    public AccountHistoryResource(CqlSession session, ParameterResource parameterResource) {
        this.session = session;
        this.parameterResource = parameterResource;
        this.mappers = new ConcurrentHashMap<>();

    }

    public List<AccountHistory> findById(UUID id) {

        return dao().findById(id).all();
    }

    public Collection<BoundStatement> createHistories(final UUID id, JsonNode source, OffsetDateTime now) {

        BinaryOperator<String> function = (n1, n2) -> n2;

        ParameterEntity parameter = parameterResource.get("default").orElseThrow(IllegalArgumentException::new);

        LOG.debug("parameters history {} {}", parameter.getApplication(), parameter.getHistories());

        Map<String, Boolean> historyFields = parameter.getHistories();

        Map<String, AccountHistory> histories = findById(id).stream().collect(Collectors.toMap(AccountHistory::getField, Function.identity()));

        AccountHistory defaultHistory = new AccountHistory();
        defaultHistory.setDate(now.toInstant().minusNanos(1));

        List<AccountHistory> accountHistories = Streams.stream(source.fields())

                .filter(e -> historyFields.containsKey(e.getKey()))

                .filter(e -> now.toInstant().isAfter(histories.getOrDefault(e.getKey(), defaultHistory).getDate()))

                .flatMap((Map.Entry<String, JsonNode> e) -> {

                    if (historyFields.get(e.getKey())) {

                        if (JsonNodeType.OBJECT == e.getValue().getNodeType()) {

                            return Streams.stream(e.getValue().fields()).map(node -> Collections
                                    .singletonMap(e.getKey().concat("/").concat(node.getKey()), node.getValue()).entrySet().iterator().next());
                        }

                        if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                            return Streams.stream(e.getValue().elements()).map((JsonNode node) -> {

                                String key = JsonNodeType.OBJECT == node.getNodeType() ? Streams.stream(node.elements())

                                        .reduce("", (root, n) -> StringHelper.join(root, n.asText(), '.'), function) : node.asText();

                                return Collections.singletonMap(e.getKey().concat("/").concat(key), node).entrySet().iterator().next();
                            });
                        }

                    }

                    return Stream.of(e);

                })

                .map((Map.Entry<String, JsonNode> e) -> {

                    AccountHistory history = new AccountHistory();
                    history.setId(id);
                    history.setField(e.getKey());
                    history.setDate(now.toInstant());
                    history.setValue(e.getValue());

                    return history;
                })

                .filter((AccountHistory history) -> !Objects.equals(history.getValue(),
                        histories.getOrDefault(history.getField(), defaultHistory).getValue()))

                .collect(Collectors.toList());

        PreparedStatement prepared = session
                .prepare("INSERT INTO " + ResourceExecutionContext.get().keyspace() + ".account_history (id,date,field,value) VALUES (?,?,?,?)");

        return accountHistories.stream()

                .map((AccountHistory history) -> {
                    LOG.debug("save history account {} {} {}", history.getId(), history.getField(), history.getValue());
                    return prepared.bind(history.getId(), history.getDate(), history.getField(), history.getValue());
                }).collect(Collectors.toList());
    }

    private AccountHistoryDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).accountHistoryDao();
    }

    private AccountHistoryMapper build(String keyspace) {

        return AccountHistoryMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
