package com.exemple.service.schema.filter.impl;

import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.FilterBuilder;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SchemaFilterImpl implements SchemaFilter {

    private final SchemaResource schemaResource;

    public SchemaFilterImpl(SchemaResource schemaResource) {

        this.schemaResource = schemaResource;
    }

    @Override
    public JsonNode filter(String app, String version, String resource, String profile, JsonNode source) {

        SchemaEntity schema = schemaResource.get(app, version, resource, profile);
        return FilterBuilder.filter(source, schema.getFilters().toArray(new String[0]));

    }

}
