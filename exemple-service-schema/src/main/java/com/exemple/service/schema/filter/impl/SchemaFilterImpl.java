package com.exemple.service.schema.filter.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.exemple.service.resource.schema.SchemaResource;
import com.exemple.service.schema.common.FilterBuilder;
import com.exemple.service.schema.filter.SchemaFilter;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class SchemaFilterImpl implements SchemaFilter {

    @Autowired
    private SchemaResource schemaResource;

    @Override
    public JsonNode filter(String app, String version, String resource, JsonNode form) {

        String[] filter = schemaResource.get(app, version, resource).getFilters().toArray(new String[0]);

        return FilterBuilder.filter(form, filter);

    }

}
