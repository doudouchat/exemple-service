package com.exemple.service.schema.filter.impl;

import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.resource.schema.model.SchemaEntity;
import com.exemple.service.schema.common.FilterBuilder;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SchemaFilterImpl implements SchemaFilter {

    private final SchemaResource schemaResource;

    @Override
    public JsonNode filter(String app, String version, String resource, String profile, JsonNode source) {

        return schemaResource.get(app, version, resource, profile)
                .map((SchemaEntity schema) -> FilterBuilder.filter(source, schema.getFilters().toArray(new String[0])))
                .orElse(source);
    }

}
