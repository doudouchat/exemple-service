package com.exemple.service.resource.core.statement;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SubscriptionStatement {

    public static final String EMAIL = "email";

    public static final String SUBSCRIPTION = "subscription";

    private final CqlSession session;

    public SubscriptionStatement(CqlSession session) {
        this.session = session;
    }

    public JsonNode get(String email) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), SUBSCRIPTION).json().all().whereColumn(EMAIL)
                .isEqualTo(QueryBuilder.literal(email));

        Row row = session.execute(select.build()).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

}
