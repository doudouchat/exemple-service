package com.exemple.service.resource.core.statement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class AccountStatement {

    public static final String TABLE = "account";

    public static final String ID = "id";

    private final CqlSession session;

    public AccountStatement(CqlSession session) {
        this.session = session;
    }

    public JsonNode get(UUID id) {

        return get(id, null);
    }

    public JsonNode get(UUID id, ConsistencyLevel consistency) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), TABLE).json().all().whereColumn(ID)
                .isEqualTo(QueryBuilder.literal(id));

        Row row = session.execute(select.build().setConsistencyLevel(consistency)).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

    public JsonNode getByIndex(String root, String field, Object value) {

        JsonNode node = JsonNodeUtils.init(root);

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), TABLE).json().all().whereColumn(field)
                .isEqualTo(QueryBuilder.literal(value));

        List<Row> rows = session.execute(select.build()).all();

        JsonNodeUtils.set(node, rows.stream().map(row -> row.get(0, JsonNode.class)).collect(Collectors.toSet()), root);

        return node;
    }

}
