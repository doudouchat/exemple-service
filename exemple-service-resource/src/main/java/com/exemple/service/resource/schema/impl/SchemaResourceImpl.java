package com.exemple.service.resource.schema.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.dao.ResourceSchemaDao;
import com.exemple.service.resource.schema.mapper.ResourceSchemaMapper;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class SchemaResourceImpl implements SchemaResource {

    private final CqlSession session;

    private final ConcurrentMap<String, ResourceSchemaMapper> mappers = new ConcurrentHashMap<>();

    @Override
    @Cacheable("schema_resource")
    public Optional<SchemaEntity> get(String app, String version, String resource, String profile) {

        return dao().findByApplicationAndResourceAndVersionAndProfile(app, resource, version, profile);
    }

    @Override
    @Cacheable("schema_resources")
    public Map<String, List<SchemaVersionProfileEntity>> allVersions(String app) {

        Map<String, List<SchemaVersionProfileEntity>> versions = new HashMap<>();

        dao().findByApplication(app).all().forEach((SchemaEntity resource) -> {
            versions.putIfAbsent(resource.getResource(), new ArrayList<>());
            versions.get(resource.getResource())
                    .add(SchemaVersionProfileEntity.builder().version(resource.getVersion()).profile(resource.getProfile()).build());
        });

        return versions;
    }

    @Override
    @CacheEvict(cacheNames = { "schema_resource", "schema_resources" }, allEntries = true)
    public void save(SchemaEntity resourceSchema) {

        dao().create(resourceSchema);

    }

    @Override
    @CacheEvict(cacheNames = { "schema_resource", "schema_resources" }, allEntries = true)
    public void update(SchemaEntity resourceSchema) {

        dao().update(resourceSchema);

    }

    private ResourceSchemaDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).resourceSchemaDao();
    }

    private ResourceSchemaMapper build(String keyspace) {

        return ResourceSchemaMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
