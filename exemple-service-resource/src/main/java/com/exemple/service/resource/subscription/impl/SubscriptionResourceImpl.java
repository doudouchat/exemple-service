package com.exemple.service.resource.subscription.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class SubscriptionResourceImpl implements SubscriptionResource {

    private static final String SUBSCRIPTION_TABLE = "subscription";

    private final CqlSession session;

    private final JsonQueryBuilder jsonQueryBuilder;

    public SubscriptionResourceImpl(CqlSession session) {

        this.session = session;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, SUBSCRIPTION_TABLE);
    }

    @Override
    public Optional<JsonNode> get(String email) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), SUBSCRIPTION_TABLE).json().all()
                .whereColumn(SubscriptionField.EMAIL.field).isEqualTo(QueryBuilder.literal(email));

        Row row = session.execute(select.build()).one();

        return Optional.ofNullable(row != null ? row.get(0, JsonNode.class) : null);
    }

    @Override
    public void save(String email, JsonNode source) {

        JsonNode subscription = JsonNodeUtils.clone(source);
        JsonNodeUtils.set(subscription, email, SubscriptionField.EMAIL.field);

        session.execute(jsonQueryBuilder.insert(subscription).build());

    }

}
