package com.exemple.service.resource.common;

import static com.exemple.service.resource.common.ResourceContext.KEYSPACE;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;

@RequiredArgsConstructor
public class JsonQueryBuilder {

    private final CqlSession session;

    private final String table;

    public Insert insert(JsonNode source) {

        return QueryBuilder.insertInto(KEYSPACE.get(), this.table).json(JsonNodeFilterUtils.clean(source),
                session.getContext().getCodecRegistry());

    }

}
