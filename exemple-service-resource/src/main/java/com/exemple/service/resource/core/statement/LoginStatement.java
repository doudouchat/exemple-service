package com.exemple.service.resource.core.statement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class LoginStatement {

    public static final String ID = "id";

    public static final String LOGIN = "login";

    private final CqlSession session;

    public LoginStatement(CqlSession session) {
        this.session = session;
    }

    public JsonNode get(String email) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), LOGIN).json().all().whereColumn(LOGIN)
                .isEqualTo(QueryBuilder.literal(email));

        Row row = session.execute(select.build()).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

    public void delete(String login) {

        session.execute(QueryBuilder.deleteFrom(ResourceExecutionContext.get().keyspace(), LOGIN).whereColumn(LOGIN)
                .isEqualTo(QueryBuilder.literal(login)).build());

    }

    public List<JsonNode> findById(UUID id) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), LOGIN).json().all().whereColumn(ID)
                .isEqualTo(QueryBuilder.literal(id));

        return session.execute(select.build()).all().stream().map((Row row) -> row.get(0, JsonNode.class)).collect(Collectors.toList());
    }

}
