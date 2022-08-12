package com.exemple.service.resource.login;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.customer.login.LoginResource;
import com.exemple.service.customer.login.UsernameAlreadyExistsException;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.login.dao.LoginDao;
import com.exemple.service.resource.login.mapper.LoginMapper;
import com.exemple.service.resource.login.model.LoginEntity;

import lombok.RequiredArgsConstructor;

@Service("loginResource")
@Validated
@RequiredArgsConstructor
public class LoginResourceImpl implements LoginResource {

    private final CqlSession session;

    private final ConcurrentMap<String, LoginMapper> mappers = new ConcurrentHashMap<>();

    @Override
    public Optional<UUID> get(String username) {

        return Optional.ofNullable(dao().findByUsername(username)).map(LoginEntity::getId);
    }

    @Override
    public void save(UUID id, String username) {

        var source = new LoginEntity();
        source.setId(id);
        source.setUsername(username);

        boolean notExists = dao().create(source);

        if (!notExists) {

            throw new UsernameAlreadyExistsException(source.getUsername());
        }

    }

    @Override
    public void delete(String username) {
        dao().deleteByUsername(username);

    }

    private LoginDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).loginDao();
    }

    private LoginMapper build(String keyspace) {

        return LoginMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
