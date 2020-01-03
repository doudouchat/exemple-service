package com.exemple.service.resource.core.statement;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.core.dao.ResourceSchemaDao;
import com.exemple.service.resource.core.mapper.ResourceSchemaMapper;
import com.exemple.service.resource.schema.model.ResourceSchema;

@Component
public class SchemaStatement {

    public static final String SCHEMA_DEFAULT = "{\"$schema\": \"http://json-schema.org/draft-07/schema\",\"additionalProperties\": false}";

    private final CqlSession session;

    private final ConcurrentMap<String, ResourceSchemaMapper> mappers;

    public SchemaStatement(CqlSession session) {
        this.session = session;
        this.mappers = new ConcurrentHashMap<>();
    }

    @Cacheable("schema_resource")
    public ResourceSchema get(String app, String version, String resource) {

        ResourceSchema resourceSchema = get().findByApplicationAndResourceAndVersion(app, resource, version);

        if (resourceSchema == null) {
            resourceSchema = new ResourceSchema();
        }

        if (resourceSchema.getContent() == null) {
            resourceSchema.setContent(SCHEMA_DEFAULT.getBytes(StandardCharsets.UTF_8));
        }

        return resourceSchema;
    }

    private ResourceSchemaDao get() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).resourceSchemaDao();
    }

    private ResourceSchemaMapper build(String keyspace) {

        return ResourceSchemaMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

    @Cacheable("schema_resources")
    public List<ResourceSchema> findByApp(String app) {

        return get().findByApplication(app).all();
    }

    public void insert(ResourceSchema resourceSchema) {

        get().create(resourceSchema);
    }

    @CacheEvict(cacheNames = { "schema_resource", "schema_resources" }, allEntries = true)
    public void update(ResourceSchema resourceSchema) {

        get().update(resourceSchema);
    }

}
