package com.exemple.service.resource.common;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.ListType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.exemple.service.resource.common.util.JsonNodeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.pivovarit.function.ThrowingConsumer;

@Component
public class JsonValidator {

    private static final Set<JsonNodeType> EXCLUDE_TYPES = EnumSet.of(JsonNodeType.NULL, JsonNodeType.MISSING);

    private static final String UNKNOWN = "UNKNOWN";

    private final CqlSession session;

    public JsonValidator(CqlSession session) {
        this.session = session;
    }

    public void valid(DataType dataType, String key, JsonNode value) throws JsonValidatorException {

        if (!EXCLUDE_TYPES.contains(value.getNodeType())) {

            if (dataType instanceof MapType) {

                this.valid((MapType) dataType, key, value);

            } else if (dataType instanceof SetType) {

                this.valid((SetType) dataType, key, value);

            } else if (dataType instanceof ListType) {

                this.valid((ListType) dataType, key, value);

            } else if (dataType instanceof UserDefinedType) {

                this.valid((UserDefinedType) dataType, value);

            } else if (dataType instanceof TupleType) {

                this.valid((TupleType) dataType, key, value);

            } else {

                valid(dataType, value.getNodeType(), value.asText(), key);
            }

        }

    }

    private void valid(MapType dataType, String key, JsonNode value) throws JsonValidatorException {

        validNodeType(value, JsonNodeType.OBJECT, key);

        JsonNodeUtils.stream(value.fields()).forEach(ThrowingConsumer.sneaky((Entry<String, JsonNode> node) -> {
            DataType keyType = dataType.getKeyType();
            DataType valueType = dataType.getValueType();

            valid(keyType, JsonNodeType.STRING, node.getKey(), node.getKey());
            valid(valueType, node.getKey(), node.getValue());

        }));
    }

    private void valid(DataType dataType, JsonNodeType type, Object value, String node) throws JsonValidatorException {

        TypeCodec<Object> typeCodec = session.getContext().getCodecRegistry().codecFor(dataType);

        Class<Object> javaType = typeCodec.getJavaType().getRawType();

        try {

            String valueString = String.valueOf(value);

            if (javaType.equals(String.class) || javaType.equals(java.time.Instant.class)) {

                valueString = new StringBuilder().append("'").append(valueString).append("'").toString();

                if (JsonNodeType.STRING != type) {
                    throw new JsonValidatorException(dataType.asCql(false, true), node);
                }

            }

            typeCodec.parse(valueString);

        } catch (IllegalArgumentException e) {
            throw new JsonValidatorException(dataType.asCql(false, true), node, e);
        }
    }

    private void valid(ListType dataType, String key, JsonNode value) throws JsonValidatorException {

        validElementType(dataType.getElementType(), key, value);
    }

    private void valid(SetType dataType, String key, JsonNode value) throws JsonValidatorException {

        validElementType(dataType.getElementType(), key, value);

    }

    private void valid(UserDefinedType dataType, JsonNode value) {

        JsonNodeUtils.stream(value.fields()).forEach(ThrowingConsumer.sneaky((Entry<String, JsonNode> node) -> valid(dataType, node)));

    }

    private void valid(UserDefinedType dataType, Entry<String, JsonNode> node) throws JsonValidatorException {

        if (!dataType.contains(node.getKey())) {
            throw new JsonValidatorException(UNKNOWN, node.getKey());
        }

        DataType type = dataType.getFieldTypes().get(dataType.firstIndexOf(node.getKey()));
        valid(type, node.getKey(), node.getValue());

    }

    private void valid(TupleType dataType, String key, JsonNode value) throws JsonValidatorException {

        Iterator<JsonNode> fields = value.elements();
        Stream<DataType> types = dataType.getComponentTypes().stream();

        types.forEach(ThrowingConsumer.sneaky((DataType type) -> valid(type, key, fields)));

        if (fields.hasNext()) {
            throw new JsonValidatorException(UNKNOWN, fields.next().asText());
        }
    }

    private void valid(DataType dataType, String key, Iterator<JsonNode> fields) throws JsonValidatorException {

        if (fields.hasNext()) {
            JsonNode node = fields.next();
            valid(dataType, key, node);
        } else {
            throw new JsonValidatorException("MSSING", key);
        }
    }

    private void validElementType(DataType type, String key, JsonNode value) throws JsonValidatorException {

        validNodeType(value, JsonNodeType.ARRAY, key);

        StreamSupport.stream(value.spliterator(), false).forEach(ThrowingConsumer.sneaky((JsonNode node) -> valid(type, key, node)));
    }

    private static void validNodeType(JsonNode value, JsonNodeType nodeType, String node) throws JsonValidatorException {

        if (nodeType != value.getNodeType()) {
            throw new JsonValidatorException(nodeType.toString(), node);
        }
    }

}
