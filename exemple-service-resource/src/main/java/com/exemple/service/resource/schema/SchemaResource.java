package com.exemple.service.resource.schema;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.schema.dao.ResourceSchemaDao;
import com.exemple.service.resource.schema.mapper.ResourceSchemaMapper;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.resource.schema.model.SchemaVersionProfileEntity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class SchemaResource {

    private final CqlSession session;

    private final ConcurrentMap<String, ResourceSchemaMapper> mappers = new ConcurrentHashMap<>();

    @Cacheable("schema_resource")
    public Optional<SchemaEntity> get(@NotBlank String resource, @NotBlank String version, @NotBlank String profile) {

        return dao().findByResourceAndVersionAndProfile(resource, version, profile);
    }

    @Cacheable("schema_resources")
    public List<SchemaVersionProfileEntity> allVersions(String resource) {

        return dao().findByResource(resource).all().stream()
                .map((SchemaEntity schema) -> SchemaVersionProfileEntity.builder().version(schema.getVersion()).profile(schema.getProfile()).build())
                .toList();
    }

    @CacheEvict(cacheNames = { "schema_resource", "schema_resources" }, allEntries = true)
    public void save(@NotNull SchemaEntity resourceSchema) {

        dao().create(resourceSchema);

    }

    @CacheEvict(cacheNames = { "schema_resource", "schema_resources" }, allEntries = true)
    public void update(@NotNull SchemaEntity resourceSchema) {

        dao().update(resourceSchema);

    }

    private ResourceSchemaDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).resourceSchemaDao();
    }

    private ResourceSchemaMapper build(String keyspace) {

        return ResourceSchemaMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
