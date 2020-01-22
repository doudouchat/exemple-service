package com.exemple.service.resource.parameter;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.parameter.dao.ParameterDao;
import com.exemple.service.resource.parameter.mapper.ParameterMapper;
import com.exemple.service.resource.parameter.model.ParameterEntity;

@Component
public class ParameterResource {

    private final CqlSession session;

    private final ConcurrentMap<String, ParameterMapper> mappers;

    public ParameterResource(CqlSession session) {
        this.session = session;
        this.mappers = new ConcurrentHashMap<>();
    }

    @Cacheable("parameter")
    public Optional<ParameterEntity> get(String application) {

        return Optional.ofNullable(dao().findByApplication(application));
    }

    private ParameterDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).parameterDao();
    }

    private ParameterMapper build(String keyspace) {

        return ParameterMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
