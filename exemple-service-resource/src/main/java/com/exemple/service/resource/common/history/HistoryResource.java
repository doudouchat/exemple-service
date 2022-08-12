package com.exemple.service.resource.common.history;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.JsonPatchUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Streams;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HistoryResource<T, E extends HistoryModel<T>> {

    private static final JsonNode DEFAULT_HISTORY_VALUE = new ObjectMapper().nullNode();

    private final HistoryModel<T> defaultHistory;

    private final HistoryDao<T, E> dao;

    private final Supplier<E> defaultHistoryBuilder;

    public HistoryResource(HistoryDao<T, E> dao, Supplier<E> defaultHistoryBuilder) {

        this.dao = dao;
        this.defaultHistoryBuilder = defaultHistoryBuilder;
        defaultHistory = defaultHistoryBuilder.get();
        defaultHistory.setValue(DEFAULT_HISTORY_VALUE);
    }

    public Collection<BoundStatement> saveHistories(T id, JsonNode source, JsonNode previousSource) {

        OffsetDateTime now = ServiceContextExecution.context().getDate();

        Map<String, HistoryModel<T>> histories = this.dao.findById(id).all().stream()
                .collect(Collectors.toMap(HistoryModel::getField, Function.identity()));

        ArrayNode patchs = JsonPatchUtils.diff(JsonNodeFilterUtils.clean(previousSource), JsonNodeFilterUtils.clean(source));

        Collection<BoundStatement> statements = new ArrayList<>();

        Streams.stream(patchs.elements())

                .map((JsonNode patch) -> buildHistory(patch, id, now, histories))

                .map((E history) -> {
                    LOG.debug("save history {} {} {}", history.getId(), history.getField(), history.getValue());
                    return this.dao.save(history);
                }).forEach(statements::add);

        histories.keySet().stream()

                .filter((String path) -> patchNotContainsPath(patchs, path))

                .filter((String path) -> JsonNodeType.MISSING == source.at(path).getNodeType())

                .map((String path) -> {
                    LOG.debug("delete history {} {}", id, path);
                    return this.dao.deleteByIdAndField(id, path);
                })

                .forEach(statements::add);

        return statements;
    }

    private E buildHistory(JsonNode patch, T id, OffsetDateTime now, Map<String, HistoryModel<T>> histories) {

        String path = patch.get(JsonPatchUtils.PATH).asText();
        JsonNode value = patch.path(JsonPatchUtils.VALUE);

        var history = this.defaultHistoryBuilder.get();
        history.setId(id);
        history.setField(path);
        history.setDate(now.toInstant());
        history.setValue(value);
        history.setPreviousValue(histories.getOrDefault(path, defaultHistory).getValue());
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

}
