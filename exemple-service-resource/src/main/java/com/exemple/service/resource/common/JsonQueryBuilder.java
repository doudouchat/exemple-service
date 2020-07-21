package com.exemple.service.resource.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.insert.Insert;
import com.datastax.oss.driver.api.querybuilder.term.Term;
import com.datastax.oss.driver.api.querybuilder.update.Assignment;
import com.datastax.oss.driver.api.querybuilder.update.UpdateStart;
import com.datastax.oss.driver.api.querybuilder.update.UpdateWithAssignments;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import com.exemple.service.resource.common.util.JsonNodeFilterUtils;
import com.exemple.service.resource.common.util.MetadataSchemaUtils;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Streams;

public class JsonQueryBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(JsonQueryBuilder.class);

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

    public Insert copy(JsonNode source, JsonNode override) {

        MetadataSchemaUtils.merge(session, table, source, override);

        return insert(source);

    }

    public UpdateWithAssignments update(JsonNode source) {

        TableMetadata tableMetadata = MetadataSchemaUtils.getTableMetadata(session, table);

        UpdateStart update = QueryBuilder.update(ResourceExecutionContext.get().keyspace(), this.table);

        List<Assignment> assignments = new ArrayList<>();
        JsonNodeFilterUtils.cleanArray(source).fields().forEachRemaining((Map.Entry<String, JsonNode> node) -> {

            DataType type = tableMetadata.getColumn(node.getKey()).get().getType();

            String cqlName = type.asCql(false, true);
            LOG.trace("{} column:{} type:{} value:{}", tableMetadata.getName(), node.getKey(), cqlName, node.getValue());

            if (type instanceof MapType) {
                assignments.addAll(updateMap((MapType) type, node));
            } else if (type instanceof SetType) {
                assignments.addAll(updateSet(node));
            } else {
                assignments.add(Assignment.setColumn(node.getKey(), json(node.getValue())));
            }

        });

        return update.set(assignments);

    }

    private List<Assignment> updateMap(MapType type, Map.Entry<String, JsonNode> node) {

        if (node.getValue().isNull()) {

            return Collections.singletonList(Assignment.setColumn(node.getKey(), json(node.getValue())));

        } else {

            List<Assignment> assignments = new ArrayList<>();
            node.getValue().fields().forEachRemaining((Map.Entry<String, JsonNode> e) -> {

                Object key = e.getKey();
                if (type.getKeyType().getProtocolCode() == ProtocolConstants.DataType.INT) {
                    key = session.getContext().getCodecRegistry().codecFor(type.getKeyType()).parse(e.getKey());
                }

                if (!e.getValue().isNull()) {

                    assignments.add(Assignment.appendMapEntry(node.getKey(), QueryBuilder.literal(key), json(e.getValue())));

                } else {

                    assignments.add(Assignment.removeSetElement(node.getKey(), QueryBuilder.literal(key)));
                }

            });

            return assignments;

        }

    }

    private List<Assignment> updateSet(Map.Entry<String, JsonNode> node) {

        return Streams.stream(node.getValue().elements()).map(v -> Assignment.appendSetElement(node.getKey(), json(v))).collect(Collectors.toList());

    }

    private Term json(JsonNode source) {

        return QueryBuilder.function("fromJson", QueryBuilder.literal(source, session.getContext().getCodecRegistry()));
    }

}
