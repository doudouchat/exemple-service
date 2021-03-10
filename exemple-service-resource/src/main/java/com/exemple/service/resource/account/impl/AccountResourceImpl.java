package com.exemple.service.resource.account.impl;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.account.AccountField;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class AccountResourceImpl implements AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResourceImpl.class);

    public static final String ACCOUNT_TABLE = "account";

    private final CqlSession session;

    private final AccountHistoryResource accountHistoryResource;

    private final JsonQueryBuilder jsonQueryBuilder;

    public AccountResourceImpl(CqlSession session, AccountHistoryResource accountHistoryResource) {
        this.session = session;
        this.accountHistoryResource = accountHistoryResource;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, ACCOUNT_TABLE);

    }

    @Override
    public UUID save(JsonNode source) {

        UUID id = UUID.randomUUID();

        JsonNodeUtils.set(source, id, AccountField.ID.field);
        save(source, JsonNodeUtils.init());

        return id;

    }

    @Override
    public void save(JsonNode source, JsonNode previousAccount) {

        Assert.isTrue(source.path(AccountField.ID.field).isTextual(), AccountField.ID.field + " is required");

        LOG.debug("save account {}", source);

        OffsetDateTime now = ServiceContextExecution.context().getDate();

        Insert account = jsonQueryBuilder.insert(source);

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(account.build());
        accountHistoryResource.saveHistories(source, previousAccount, now).forEach(batch::addStatements);

        session.execute(batch.build());

    }

    @Override
    public Optional<JsonNode> get(UUID id) {

        Optional<JsonNode> source = getById(id);

        source.ifPresent(node -> LOG.debug("get account {} {}", id, node));

        return source;
    }

    private Optional<JsonNode> getById(UUID id) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), ACCOUNT_TABLE).json().all()
                .whereColumn(AccountField.ID.field).isEqualTo(QueryBuilder.literal(id));

        Row row = session.execute(select.build().setConsistencyLevel(DefaultConsistencyLevel.QUORUM)).one();

        return Optional.ofNullable(row != null ? row.get(0, JsonNode.class) : null);
    }

    @Override
    public Set<JsonNode> findByIndex(String index, Object value) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), ACCOUNT_TABLE).json().all().whereColumn(index)
                .isEqualTo(QueryBuilder.literal(value));

        return session.execute(select.build()).all().stream().map(row -> row.get(0, JsonNode.class)).collect(Collectors.toSet());
    }
}
