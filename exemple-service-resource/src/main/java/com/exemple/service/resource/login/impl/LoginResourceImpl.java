package com.exemple.service.resource.login.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.exemple.service.resource.common.JsonQueryBuilder;
import com.exemple.service.resource.core.statement.LoginStatement;
import com.exemple.service.resource.login.LoginResource;
import com.exemple.service.resource.login.exception.LoginResourceExistException;
import com.fasterxml.jackson.databind.JsonNode;

@Service
@Validated
public class LoginResourceImpl implements LoginResource {

    private final CqlSession session;

    private final LoginStatement loginStatement;

    private final JsonQueryBuilder jsonQueryBuilder;

    public LoginResourceImpl(LoginStatement loginStatement, CqlSession session) {
        this.loginStatement = loginStatement;
        this.session = session;
        this.jsonQueryBuilder = new JsonQueryBuilder(session, LoginStatement.LOGIN);

    }

    @Override
    public Optional<JsonNode> get(String login) {

        JsonNode source = loginStatement.get(login);

        return Optional.ofNullable(source);
    }

    @Override
    public void save(String login, JsonNode source) {

        JsonNode data = loginStatement.get(login);

        if (data == null) {

            session.execute(update(login, source).build());

        } else {

            loginStatement.findById(UUID.fromString(data.get(LoginStatement.ID).textValue())).stream()
                    .map((JsonNode l) -> l.get(LoginStatement.LOGIN).textValue()).forEach((String l) -> session.execute(update(l, source).build()));
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

        loginStatement.delete(login);
    }

    private Update update(String login, JsonNode source) {

        return jsonQueryBuilder.update(source).whereColumn(LoginStatement.LOGIN).isEqualTo(QueryBuilder.literal(login));
    }

}
