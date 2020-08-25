package com.exemple.service.resource.common;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

public class JsonQueryBuilder {

    private final String table;

    private final CqlSession session;

    public JsonQueryBuilder(CqlSession session, String table) {
        this.table = table;
        this.session = session;
    }

    public Insert insert(JsonNode source) {

        return QueryBuilder.insertInto(ResourceExecutionContext.get().keyspace(), this.table).json(JsonNodeFilterUtils.clean(source),
                session.getContext().getCodecRegistry());

    }

}
