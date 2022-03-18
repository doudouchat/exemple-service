package com.exemple.service.resource.account.history;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
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
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.resource.account.history.dao.AccountHistoryDao;
import com.exemple.service.resource.account.history.mapper.AccountHistoryMapper;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonPatchUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Streams;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountHistoryResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountHistoryResource.class);

    private static final JsonNode DEFAULT_HISTORY_VALUE = new ObjectMapper().nullNode();

    private static final AccountHistory DEFAULT_HISTORY;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {

        DEFAULT_HISTORY = new AccountHistory();
        DEFAULT_HISTORY.setValue(DEFAULT_HISTORY_VALUE);
    }

    private final CqlSession session;

    private final ConcurrentMap<String, AccountHistoryMapper> mappers = new ConcurrentHashMap<>();

    public List<AccountHistory> findById(UUID id) {

        return dao().findById(id).all();
    }

    public AccountHistory findByIdAndField(UUID id, String field) {

        return dao().findByIdAndField(id, field);
    }

    public Collection<BoundStatement> saveHistories(JsonNode source) {

        return saveHistories(source, MAPPER.createObjectNode());
    }

    public Collection<BoundStatement> saveHistories(JsonNode source, JsonNode previousSource) {

        OffsetDateTime now = ServiceContextExecution.context().getDate();

        UUID id = UUID.fromString(source.get(AccountField.ID.field).textValue());

        Map<String, AccountHistory> histories = findById(id).stream().collect(Collectors.toMap(AccountHistory::getField, Function.identity()));

        ArrayNode patchs = JsonPatchUtils.diff(JsonNodeFilterUtils.clean(previousSource), JsonNodeFilterUtils.clean(source));

        Collection<BoundStatement> statements = new ArrayList<>();

        Streams.stream(patchs.elements())

                .map((JsonNode patch) -> buildHistory(patch, id, now, histories))

                .map((AccountHistory history) -> {
                    LOG.debug("save history account {} {} {}", history.getId(), history.getField(), history.getValue());
                    return dao().save(history);
                }).forEach(statements::add);

        histories.keySet().stream()

                .filter((String path) -> patchNotContainsPath(patchs, path))

                .filter((String path) -> JsonNodeType.MISSING == source.at(path).getNodeType())

                .map((String path) -> {
                    LOG.debug("delete history account {} {}", id, path);
                    return dao().deleteByIdAndField(id, path);
                })

                .forEach(statements::add);

        return statements;
    }

    private static AccountHistory buildHistory(JsonNode patch, UUID id, OffsetDateTime now, Map<String, AccountHistory> histories) {

        String path = patch.get(JsonPatchUtils.PATH).asText();
        JsonNode value = patch.path(JsonPatchUtils.VALUE);

        AccountHistory history = new AccountHistory();
        history.setId(id);
        history.setField(path);
        history.setDate(now.toInstant());
        history.setValue(value);
        history.setPreviousValue(histories.getOrDefault(path, DEFAULT_HISTORY).getValue());
        history.setApplication(ServiceContextExecution.context().getApp());
        history.setVersion(ServiceContextExecution.context().getVersion());
        history.setUser(ServiceContextExecution.context().getPrincipal().getName());

        if (JsonPatchUtils.isRemoveOperation(patch)) {
            history.setValue(DEFAULT_HISTORY_VALUE);
            history.setPreviousValue(value);
        }

        return history;
    }

    private static boolean patchNotContainsPath(ArrayNode patch, String path) {

        return Streams.stream(patch.elements()).noneMatch((JsonNode element) -> path.equals(element.get(JsonPatchUtils.PATH).textValue()));
    }

    private AccountHistoryDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).accountHistoryDao();
    }

    private AccountHistoryMapper build(String keyspace) {

        return AccountHistoryMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
