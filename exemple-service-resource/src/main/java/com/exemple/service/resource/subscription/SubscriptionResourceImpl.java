package com.exemple.service.resource.subscription;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.customer.subscription.SubscriptionResource;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.common.model.EventType;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.subscription.event.SubscriptionEventResource;
import com.exemple.service.resource.subscription.history.SubscriptionHistoryResource;

import tools.jackson.databind.JsonNode;

@Service("subscriptionResource")
@Validated
public class SubscriptionResourceImpl implements SubscriptionResource {

    private static final String SUBSCRIPTION_TABLE = "subscription";

    private final CqlSession session;

    private final JsonQueryBuilder jsonQueryBuilder;

    private final SubscriptionHistoryResource subscriptionHistoryResource;

    private final SubscriptionEventResource subscriptionEventResource;

    public SubscriptionResourceImpl(CqlSession session, SubscriptionHistoryResource subscriptionHistoryResource,
            SubscriptionEventResource subscriptionEventResource) {

        this.session = session;
        this.subscriptionHistoryResource = subscriptionHistoryResource;
        this.subscriptionEventResource = subscriptionEventResource;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, SUBSCRIPTION_TABLE);
    }

    @Override
    public Optional<JsonNode> get(String email) {

        var select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), SUBSCRIPTION_TABLE).json().all()
                .whereColumn(SubscriptionField.EMAIL.field).isEqualTo(QueryBuilder.literal(email));

        var row = session.execute(select.build()).one();

        return Optional.ofNullable(row != null ? row.get(0, JsonNode.class) : null);
    }

    @Override
    public void create(JsonNode subscription) {

        Assert.isTrue(subscription.path(SubscriptionField.EMAIL.field).isString(), SubscriptionField.EMAIL.field + " is required");

        var createSubscription = jsonQueryBuilder.insert(subscription);

        var batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(createSubscription.build());
        subscriptionHistoryResource.saveHistories(subscription).forEach(batch::addStatements);
        batch.addStatement(subscriptionEventResource.saveEvent(subscription, EventType.CREATE));

        session.execute(batch.build());

    }

    @Override
    public void update(JsonNode subscription) {

        Assert.isTrue(subscription.path(SubscriptionField.EMAIL.field).isString(), SubscriptionField.EMAIL.field + " is required");

        var createSubscription = jsonQueryBuilder.insert(subscription);

        var batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(createSubscription.build());
        subscriptionHistoryResource.saveHistories(subscription).forEach(batch::addStatements);
        batch.addStatement(subscriptionEventResource.saveEvent(subscription, EventType.UPDATE));

        session.execute(batch.build());

    }

    @Override
    public void delete(String email) {

        var deleteSubscription = QueryBuilder.deleteFrom(ResourceExecutionContext.get().keyspace(), SUBSCRIPTION_TABLE)
                .whereColumn(SubscriptionField.EMAIL.field).isEqualTo(QueryBuilder.literal(email));

        var batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(deleteSubscription.build());
        batch.addStatement(subscriptionEventResource.saveEvent(email, EventType.DELETE));

        session.execute(batch.build());
    }

}
