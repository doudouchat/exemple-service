package com.exemple.service.resource.subscription.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.delete.Delete;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.SubscriptionField;
import com.exemple.service.resource.subscription.SubscriptionResource;
import com.exemple.service.resource.subscription.event.SubscriptionEventResource;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class SubscriptionResourceImpl implements SubscriptionResource {

    private static final String SUBSCRIPTION_TABLE = "subscription";

    private final CqlSession session;

    private final JsonQueryBuilder jsonQueryBuilder;

    private final SubscriptionEventResource subscriptionEventResource;

    public SubscriptionResourceImpl(CqlSession session, SubscriptionEventResource subscriptionEventResource) {

        this.session = session;
        this.subscriptionEventResource = subscriptionEventResource;
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
    public void save(JsonNode subscription) {

        Assert.isTrue(subscription.path(SubscriptionField.EMAIL.field).isTextual(), SubscriptionField.EMAIL.field + " is required");

        Insert createSubscription = jsonQueryBuilder.insert(subscription);

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(createSubscription.build());
        batch.addStatement(subscriptionEventResource.saveEvent(subscription, EventType.CREATE));

        session.execute(batch.build());

    }

    @Override
    public void delete(String email) {

        Delete deleteSubscription = QueryBuilder.deleteFrom(ResourceExecutionContext.get().keyspace(), SUBSCRIPTION_TABLE)
                .whereColumn(SubscriptionField.EMAIL.field).isEqualTo(QueryBuilder.literal(email));

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(deleteSubscription.build());
        batch.addStatement(subscriptionEventResource.saveEvent(email, EventType.DELETE));

        session.execute(batch.build());
    }

}
