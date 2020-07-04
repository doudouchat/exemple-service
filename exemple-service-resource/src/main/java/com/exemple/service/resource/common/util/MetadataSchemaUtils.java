package com.exemple.service.resource.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Streams;

public final class MetadataSchemaUtils {

    private MetadataSchemaUtils() {

    }

    public static TableMetadata getTableMetadata(Session session, String table) {

        return session.getMetadata().getKeyspace(ResourceExecutionContext.get().keyspace()).orElseThrow(IllegalStateException::new).getTable(table)
                .orElseThrow(IllegalStateException::new);
    }

    public static Collection<Pair<String, JsonNode>> transformColumnToLine(Session session, String table, JsonNode source) {

        TableMetadata tableMetadata = getTableMetadata(session, table);

        return Streams.stream(source.fields())

                .flatMap((Map.Entry<String, JsonNode> e) -> {

                    DataType type = tableMetadata.getColumn(e.getKey()).get().getType();

                    if (type instanceof MapType && e.getValue().isObject()) {

                        return Streams.stream(e.getValue().fields()).map(node -> Collections
                                .singletonMap(e.getKey().concat("/").concat(node.getKey()), node.getValue()).entrySet().iterator().next());
                    }

                    return Stream.of(e);

                })

                .map((Map.Entry<String, JsonNode> e) -> Pair.of(e.getKey(), e.getValue())).collect(Collectors.toList());
    }

    public static void merge(Session session, String table, JsonNode source, JsonNode override) {

        TableMetadata tableMetadata = getTableMetadata(session, table);

        override.fields().forEachRemaining((Map.Entry<String, JsonNode> node) -> {

            DataType type = tableMetadata.getColumn(node.getKey()).get().getType();

            if (type instanceof SetType) {

                ((ArrayNode) override.get(node.getKey())).addAll((ArrayNode) source.get(node.getKey()));

            }

            JsonNodeUtils.set(source, override.get(node.getKey()), node.getKey());

        });

    }

}
