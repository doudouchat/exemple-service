package com.exemple.service.resource.account.history;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.exemple.service.resource.account.model.AccountHistory;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.common.util.StringHelper;
import com.exemple.service.resource.core.statement.AccountHistoryStatement;
import com.exemple.service.resource.core.statement.ParameterStatement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Component
public class AccountHistoryResource {

    private final AccountHistoryStatement accountHistoryStatement;

    private final ParameterStatement parameterStatement;

    public AccountHistoryResource(AccountHistoryStatement accountHistoryStatement, ParameterStatement parameterStatement) {
        this.accountHistoryStatement = accountHistoryStatement;
        this.parameterStatement = parameterStatement;
    }

    public Collection<BoundStatement> createHistories(final UUID id, JsonNode source, OffsetDateTime now) {

        BinaryOperator<String> function = (n1, n2) -> n2;

        Map<String, Boolean> historyFields = parameterStatement.getHistories();

        Map<String, AccountHistory> histories = accountHistoryStatement.findById(id).stream()
                .collect(Collectors.toMap(AccountHistory::getField, Function.identity()));

        AccountHistory defaultHistory = new AccountHistory();
        defaultHistory.setDate(now.toInstant().minusNanos(1));

        List<AccountHistory> accountHistories = JsonNodeUtils.stream(source.fields())

                .filter(e -> historyFields.containsKey(e.getKey()))

                .filter(e -> now.toInstant().isAfter(histories.getOrDefault(e.getKey(), defaultHistory).getDate()))

                .flatMap((Map.Entry<String, JsonNode> e) -> {

                    if (historyFields.get(e.getKey())) {

                        if (JsonNodeType.OBJECT == e.getValue().getNodeType()) {

                            return JsonNodeUtils.stream(e.getValue().fields()).map(node -> Collections
                                    .singletonMap(e.getKey().concat("/").concat(node.getKey()), node.getValue()).entrySet().iterator().next());
                        }

                        if (JsonNodeType.ARRAY == e.getValue().getNodeType()) {

                            return JsonNodeUtils.stream(e.getValue().elements()).map((JsonNode node) -> {

                                String key = JsonNodeType.OBJECT == node.getNodeType() ? JsonNodeUtils.stream(node.elements())

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

        return accountHistoryStatement.insert(accountHistories);
    }

}
