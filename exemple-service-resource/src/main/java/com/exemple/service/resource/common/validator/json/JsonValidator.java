package com.exemple.service.resource.common.validator.json;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.ListType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.internal.core.type.PrimitiveType;
import com.exemple.service.resource.common.JsonValidatorException;
import com.exemple.service.resource.common.util.MetadataSchemaUtils;
import com.google.common.collect.Maps;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

@Component
@RequiredArgsConstructor
public class JsonValidator {

    private static final String UNKNOWN_EXCEPTION = "UNKNOWN";

    private static final String ARRAY_EXCEPTION = "ARRAY";

    private final CqlSession session;

    public void valid(JsonNode source, String table) throws JsonValidatorException {

        JsonStreamer<Map.Entry<String, JsonNode>> fields = source.properties()::iterator;

        fields.forEach((Map.Entry<String, JsonNode> node) -> {

            var type = toDataType(node.getKey(), table);

            this.valid(type, node);
        });

    }

    private void valid(DataType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        if (json.getValue().isNull()) {
            return;
        }

        JsonConsumer<Map.Entry<String, JsonNode>> validate = switch (dataType) {
            case MapType type -> (Map.Entry<String, JsonNode> j) -> validateMap(type, j);
            case ListType type -> (Map.Entry<String, JsonNode> j) -> validateList(type, j);
            case SetType type -> (Map.Entry<String, JsonNode> j) -> validateSet(type, j);
            case UserDefinedType type -> (Map.Entry<String, JsonNode> j) -> validateUDT(type, j);
            case TupleType type -> (Map.Entry<String, JsonNode> j) -> validateTuple(type, j);
            case PrimitiveType type when isStringOrInstant(dataType) -> (Map.Entry<String, JsonNode> j) -> validateStringOrInstant(type, j);
            default -> (Map.Entry<String, JsonNode> j) -> validateDefault(dataType, j);
        };

        validate.accept(json);

    }

    private void validateMap(MapType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        if (!json.getValue().isObject()) {
            throw new JsonValidatorException("OBJECT", json.getKey());
        }

        JsonStreamer<Map.Entry<String, JsonNode>> fields = json.getValue().properties()::iterator;

        fields.forEach((Entry<String, JsonNode> node) -> {
            var keyType = dataType.getKeyType();
            valid(keyType, Maps.immutableEntry(json.getKey(), new StringNode(node.getKey())));

            var valueType = dataType.getValueType();
            valid(valueType, node);

        });

    }

    private void validateList(ListType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        if (!json.getValue().isArray()) {
            throw new JsonValidatorException(ARRAY_EXCEPTION, json.getKey());
        }

        JsonStreamer<JsonNode> elements = json.getValue()::iterator;

        elements.forEach((JsonNode node) -> valid(dataType.getElementType(), Maps.immutableEntry(json.getKey(), node)));

    }

    private void validateSet(SetType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        if (!json.getValue().isArray()) {
            throw new JsonValidatorException(ARRAY_EXCEPTION, json.getKey());
        }

        JsonStreamer<JsonNode> elements = json.getValue()::iterator;

        elements.forEach((JsonNode node) -> valid(dataType.getElementType(), Maps.immutableEntry(json.getKey(), node)));

    }

    private void validateUDT(UserDefinedType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        JsonStreamer<Entry<String, JsonNode>> fields = json.getValue().properties()::iterator;

        fields.forEach((Entry<String, JsonNode> node) -> {

            if (!dataType.contains(node.getKey())) {
                throw new JsonValidatorException(UNKNOWN_EXCEPTION, node.getKey());
            }

            DataType type = dataType.getFieldTypes().get(dataType.firstIndexOf(node.getKey()));
            valid(type, node);
        });

    }

    private void validateTuple(TupleType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        Iterator<DataType> dataTypes = dataType.getComponentTypes().iterator();

        JsonStreamer<JsonNode> elements = json.getValue()::iterator;

        elements.forEach((JsonNode node) -> {

            if (!dataTypes.hasNext()) {
                throw new JsonValidatorException(UNKNOWN_EXCEPTION, json.getKey());
            }

            valid(dataTypes.next(), Maps.immutableEntry(json.getKey(), node));

        });

        if (dataTypes.hasNext()) {
            throw new JsonValidatorException("MISSING", json.getKey());
        }

    }

    private void validateStringOrInstant(PrimitiveType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        if (!json.getValue().isString()) {
            throw new JsonValidatorException("VARCHAR", json.getKey());
        }

        var value = new StringBuilder().append("'").append(json.getValue().asString()).append("'").toString();
        checkDataType(value, dataType, json);

    }

    private void validateDefault(DataType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        checkDataType(json.getValue().asString(), dataType, json);

    }

    private void checkDataType(String value, DataType dataType, Entry<String, JsonNode> json) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = toTypeCodec(dataType);
        try {
            typeCodec.parse(value);
        } catch (IllegalArgumentException e) {
            throw new JsonValidatorException(typeCodec.getCqlType().asCql(true, false), json.getKey(), e);
        }
    }

    private boolean isStringOrInstant(DataType dataType) {
        TypeCodec<Object> typeCodec = toTypeCodec(dataType);
        Class<Object> javaType = typeCodec.getJavaType().getRawType();
        return javaType.equals(String.class) || javaType.equals(java.time.Instant.class);
    }

    private TypeCodec<Object> toTypeCodec(DataType dataType) {

        return session.getContext().getCodecRegistry().codecFor(dataType);
    }

    private DataType toDataType(String key, String table) throws JsonValidatorException {

        var tableMetadata = MetadataSchemaUtils.getTableMetadata(session, table);
        return tableMetadata.getColumn(key).orElseThrow(() -> new JsonValidatorException(UNKNOWN_EXCEPTION, key)).getType();
    }

    @FunctionalInterface
    private interface JsonStreamer<T> extends Supplier<Iterator<T>> {

        default void forEach(JsonConsumer<T> action) throws JsonValidatorException {
            Iterator<T> nodes = get();
            while (nodes.hasNext()) {
                action.accept(nodes.next());
            }
        }

    }

    @FunctionalInterface
    private interface JsonConsumer<T> {

        void accept(T node) throws JsonValidatorException;

    }

}
