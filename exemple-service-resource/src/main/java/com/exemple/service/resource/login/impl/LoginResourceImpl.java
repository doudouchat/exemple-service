package com.exemple.service.resource.login.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

@Service
@Validated
public class LoginResourceImpl implements LoginResource {

    private static final String LOGIN_TABLE = "login";

    private final CqlSession session;

    private final JsonQueryBuilder jsonQueryBuilder;

    public LoginResourceImpl(CqlSession session) {
        this.session = session;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, LOGIN_TABLE);

    }

    @Override
    public Optional<JsonNode> get(String username) {

        return Optional.ofNullable(getByUsername(username));
    }

    @Override
    public void save(String username, JsonNode source) throws LoginResourceExistException {

        if (source.path(LoginField.USERNAME.field).getNodeType() == JsonNodeType.MISSING) {

            Update update = updateLogin(username, source);

            session.execute(update.build());

        } else {

            String newUsername = source.get(LoginField.USERNAME.field).textValue();

            BatchStatementBuilder batch = new BatchStatementBuilder(BatchType.LOGGED);
            batch.addStatements(replaceUsername(username, newUsername, source));

            Row resultLogin = session.execute(batch.build()).one();
            boolean notExistLogin = resultLogin.getBoolean(0);

            if (!notExistLogin) {
                throw new LoginResourceExistException(resultLogin.getString(1));
            }

            this.delete(username);

        }

    }

    @Override
    public void save(JsonNode source) throws LoginResourceExistException {

        Insert insert = insertLogin(source);

        Row resultLogin = session.execute(insert.build()).one();
        boolean notExistLogin = resultLogin.getBoolean(0);

        if (!notExistLogin) {
            throw new LoginResourceExistException(resultLogin.getString(1));
        }
    }

    @Override
    public void delete(String username) {

        session.execute(QueryBuilder.deleteFrom(ResourceExecutionContext.get().keyspace(), LOGIN_TABLE).whereColumn(LoginField.USERNAME.field)
                .isEqualTo(QueryBuilder.literal(username)).build());
    }

    @Override
    public List<JsonNode> get(UUID id) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), LOGIN_TABLE).json().all().whereColumn(LoginField.ID.field)
                .isEqualTo(QueryBuilder.literal(id));

        return session.execute(select.build()).all().stream().map((Row row) -> row.get(0, JsonNode.class)).collect(Collectors.toList());
    }

    private Insert insertLogin(JsonNode source) {

        return jsonQueryBuilder.insert(source).ifNotExists();
    }

    private List<BatchableStatement<?>> replaceUsername(String previousUsername, String nextUsername, JsonNode source) {

        List<BatchableStatement<?>> statements = new ArrayList<>();

        JsonNode login = getByUsername(previousUsername);
        JsonNodeUtils.set(login, nextUsername, LoginField.USERNAME.field);
        statements.add(insertLogin(login).build());

        JsonNode sourceExceptUsername = JsonNodeUtils.clone(source, LoginField.USERNAME.field);
        if (!sourceExceptUsername.isEmpty()) {
            statements.add(updateLogin(nextUsername, sourceExceptUsername).build());
        }

        return statements;

    }

    private Update updateLogin(String username, JsonNode source) {

        return jsonQueryBuilder.update(source).whereColumn(LoginField.USERNAME.field).isEqualTo(QueryBuilder.literal(username));
    }

    private JsonNode getByUsername(String username) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), LOGIN_TABLE).json().all()
                .whereColumn(LoginField.USERNAME.field).isEqualTo(QueryBuilder.literal(username));

        Row row = session.execute(select.build()).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

}
