package com.exemple.service.resource.core.statement;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.select.Selector;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class ParameterStatement {

    public static final String TABLE = "parameter";

    public static final String APP = "app";

    public static final String APP_DEFAULT = "default";

    private final CqlSession session;

    public ParameterStatement(CqlSession session) {
        this.session = session;
    }

    public JsonNode get(String parameter) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), TABLE).function("toJson", Selector.column(parameter))
                .whereColumn(APP).isEqualTo(QueryBuilder.literal(APP_DEFAULT));

        return Optional.of(session.execute(select.build()).one()).orElseThrow(IllegalArgumentException::new).get(0, JsonNode.class);
    }

    @Cacheable("parameter_histories")
    public Map<String, Boolean> getHistories() {
        return JsonNodeUtils.stream(this.get("histories").fields()).collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().booleanValue()));
    }

}
