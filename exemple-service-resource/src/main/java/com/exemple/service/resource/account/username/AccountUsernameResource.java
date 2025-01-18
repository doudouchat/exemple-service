package com.exemple.service.resource.account.username;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.account.model.AccountUsername;
import com.exemple.service.resource.account.username.dao.AccountUsernameDao;
import com.exemple.service.resource.account.username.mapper.AccountUsernameMapper;
import com.exemple.service.resource.core.ResourceExecutionContext;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AccountUsernameResource {

    private final CqlSession session;

    private final ConcurrentMap<String, AccountUsernameMapper> mappers = new ConcurrentHashMap<>();

    public Optional<UUID> findByUsernameAndField(String username, String field) {

        return dao().findByUsernameAndField(username, field).map(AccountUsername::getId);
    }

    public void save(String username, String field, UUID id) {

        var accountUsername = new AccountUsername(username, field, id);
        dao().save(accountUsername);
    }

    public void delete(String username, String field) {

        dao().deleteByUsernameAndField(username, field);
    }

    private AccountUsernameDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).accountUsernameDao();
    }

    private AccountUsernameMapper build(String keyspace) {

        return AccountUsernameMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
