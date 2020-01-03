package com.exemple.service.resource.account.impl;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.exemple.service.resource.account.AccountResource;
import com.exemple.service.resource.account.history.AccountHistoryResource;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.statement.AccountStatement;
import com.exemple.service.resource.core.statement.LoginStatement;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class AccountResourceImpl implements AccountResource {

    private static final Logger LOG = LoggerFactory.getLogger(AccountResourceImpl.class);

    private static final String STATUS = "status";

    private final CqlSession session;

    private final AccountStatement accountStatement;

    private final AccountHistoryResource accountHistoryResource;

    private final JsonQueryBuilder jsonQueryBuilder;

    public AccountResourceImpl(CqlSession session, AccountStatement accountStatement, AccountHistoryResource accountHistoryResource) {
        this.session = session;
        this.accountStatement = accountStatement;
        this.accountHistoryResource = accountHistoryResource;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, AccountStatement.TABLE);

    }

    @Override
    public JsonNode save(UUID id, JsonNode source) {

        LOG.debug("save account {} {}", id, source);

        OffsetDateTime now = ResourceExecutionContext.get().getDate();

        JsonNode accountNode = JsonNodeUtils.clone(source);
        JsonNodeUtils.set(accountNode, id, LoginStatement.ID);
        Insert account = jsonQueryBuilder.insert(accountNode);

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.addStatement(account.build());
        accountHistoryResource.createHistories(id, accountNode, now).forEach(batch::addStatements);

        session.execute(batch.build());

        return accountNode;

    }

    @Override
    public JsonNode update(UUID id, JsonNode source) {

        LOG.debug("update account {} {}", id, source);

        OffsetDateTime now = ResourceExecutionContext.get().getDate();

        Update update = jsonQueryBuilder.update(source).whereColumn(AccountStatement.ID).isEqualTo(QueryBuilder.literal(id));

        BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
        batch.setConsistencyLevel(DefaultConsistencyLevel.QUORUM);
        batch.addStatement(update.build());
        accountHistoryResource.createHistories(id, source, now).forEach(batch::addStatements);

        session.execute(batch.build());

        return this.getById(id).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public Optional<JsonNode> get(UUID id) {

        Optional<JsonNode> source = getById(id);

        source.ifPresent(node -> LOG.debug("get account {} {}", id, node));

        return source;
    }

    private Optional<JsonNode> getById(UUID id) {

        JsonNode source = accountStatement.get(id, DefaultConsistencyLevel.QUORUM);

        return Optional.ofNullable(source);
    }

    @Override
    public JsonNode getByStatus(String status) {

        JsonNode node = accountStatement.getByIndex("accounts", STATUS, status);

        LOG.debug("get account by status {}:{}", status, node);

        return node;
    }
}
