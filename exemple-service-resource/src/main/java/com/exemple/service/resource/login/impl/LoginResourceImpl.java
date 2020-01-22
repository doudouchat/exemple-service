package com.exemple.service.resource.login.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.login.LoginField;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

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
    public Optional<JsonNode> get(String login) {

        JsonNode source = getByLogin(login);

        return Optional.ofNullable(source);
    }

    @Override
    public void save(String login, JsonNode source) {

        JsonNode data = getByLogin(login);

        if (data == null) {

            session.execute(update(login, source).build());

        } else {

            Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), LOGIN_TABLE).json().all()
                    .whereColumn(LoginField.ID.field).isEqualTo(QueryBuilder.literal(UUID.fromString(data.get(LoginField.ID.field).textValue())));

            session.execute(select.build()).all().stream().map((Row row) -> row.get(0, JsonNode.class))
                    .map((JsonNode l) -> l.get(LoginField.LOGIN.field).textValue()).forEach((String l) -> session.execute(update(l, source).build()));
        }

    }

    @Override
    public void save(JsonNode source) throws LoginResourceExistException {

        Insert insert = jsonQueryBuilder.insert(source).ifNotExists();

        Row resultLogin = session.execute(insert.build()).one();
        boolean notExistLogin = resultLogin.getBoolean(0);

        if (!notExistLogin) {
            throw new LoginResourceExistException(resultLogin.getString(1));
        }
    }

    @Override
    public void delete(String login) {

        session.execute(QueryBuilder.deleteFrom(ResourceExecutionContext.get().keyspace(), LOGIN_TABLE).whereColumn(LoginField.LOGIN.field)
                .isEqualTo(QueryBuilder.literal(login)).build());
    }

    private JsonNode getByLogin(String login) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), LOGIN_TABLE).json().all()
                .whereColumn(LoginField.LOGIN.field).isEqualTo(QueryBuilder.literal(login));

        Row row = session.execute(select.build()).one();

        return row != null ? row.get(0, JsonNode.class) : null;
    }

    private Update update(String login, JsonNode source) {

        return jsonQueryBuilder.update(source).whereColumn(LoginField.LOGIN.field).isEqualTo(QueryBuilder.literal(login));
    }

}
