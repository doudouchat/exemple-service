package com.exemple.service.resource.common.validator.json;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.metadata.schema.ColumnMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.ListType;
import com.datastax.oss.driver.api.core.type.MapType;
import com.datastax.oss.driver.api.core.type.SetType;
import com.datastax.oss.driver.api.core.type.TupleType;
import com.datastax.oss.driver.api.core.type.UserDefinedType;
import com.datastax.oss.protocol.internal.ProtocolConstants;
import com.exemple.service.resource.common.JsonValidatorException;
import com.exemple.service.resource.common.util.MetadataSchemaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.pivovarit.function.ThrowingConsumer;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JsonValidator {

    private static final Set<JsonNodeType> EXCLUDE_TYPES = EnumSet.of(JsonNodeType.NULL, JsonNodeType.MISSING);

    private static final String UNKNOWN_EXCEPTION = "UNKNOWN";

    private static final String ARRAY_EXCEPTION = "ARRAY";

    private final CqlSession session;

    private final JsonValidatorHelper jsonValidatorHelper;

    public void valid(JsonNode source, String table) throws JsonValidatorException {

        ThrowingConsumer<Map.Entry<String, JsonNode>, JsonValidatorException> validDataTypeAndJsonNode = (Map.Entry<String, JsonNode> node) -> {

            DataType type = toDataType(node.getKey(), table);

            this.valid(type, node);
        };

        Streams.stream(source.fields()).forEach(ThrowingConsumer.sneaky(validDataTypeAndJsonNode));

    }

    private void valid(DataType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        if (!EXCLUDE_TYPES.contains(json.getValue().getNodeType())) {

            switch (dataType.getProtocolCode()) {
                case ProtocolConstants.DataType.MAP:
                    this.validMapType((MapType) dataType, json);
                    break;
                case ProtocolConstants.DataType.SET:
                    this.validSetType((SetType) dataType, json);
                    break;
                case ProtocolConstants.DataType.LIST:
                    this.validListType((ListType) dataType, json);
                    break;
                case ProtocolConstants.DataType.UDT:
                    this.validUserDefinedType((UserDefinedType) dataType, json);
                    break;
                case ProtocolConstants.DataType.TUPLE:
                    this.validTupleType((TupleType) dataType, json);
                    break;
                default:
                    this.validDefaultType(dataType, json);
            }

        }

    }

    private void validMapType(MapType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        JsonValidatorHelper.checkIfCondition(JsonNodeType.OBJECT == json.getValue().getNodeType(), "OBJECT", json.getKey());

        Streams.stream(json.getValue().fields()).forEach(ThrowingConsumer.sneaky((Entry<String, JsonNode> node) -> {
            DataType keyType = dataType.getKeyType();
            jsonValidatorHelper.valid(keyType, node.getKey(), json.getKey());

            DataType valueType = dataType.getValueType();
            valid(valueType, node);

        }));
    }

    private void validListType(ListType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        JsonValidatorHelper.checkIfCondition(JsonNodeType.ARRAY == json.getValue().getNodeType(), ARRAY_EXCEPTION, json.getKey());

        Streams.stream(json.getValue())
                .forEach(ThrowingConsumer.sneaky((JsonNode node) -> valid(dataType.getElementType(), Maps.immutableEntry(json.getKey(), node))));
    }

    private void validSetType(SetType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        JsonValidatorHelper.checkIfCondition(JsonNodeType.ARRAY == json.getValue().getNodeType(), ARRAY_EXCEPTION, json.getKey());

        Streams.stream(json.getValue())
                .forEach(ThrowingConsumer.sneaky((JsonNode node) -> valid(dataType.getElementType(), Maps.immutableEntry(json.getKey(), node))));
    }

    private void validUserDefinedType(UserDefinedType dataType, Map.Entry<String, JsonNode> json) {

        Streams.stream(json.getValue().fields()).forEach(ThrowingConsumer.sneaky((Entry<String, JsonNode> node) -> {

            JsonValidatorHelper.checkIfCondition(dataType.contains(node.getKey()), UNKNOWN_EXCEPTION, json.getKey());

            DataType type = dataType.getFieldTypes().get(dataType.firstIndexOf(node.getKey()));
            valid(type, node);
        }));

    }

    private void validTupleType(TupleType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        Iterator<DataType> dataTypes = dataType.getComponentTypes().iterator();

        Streams.stream(json.getValue()).forEach(ThrowingConsumer.sneaky((JsonNode node) -> {

            JsonValidatorHelper.checkIfCondition(dataTypes.hasNext(), UNKNOWN_EXCEPTION, json.getKey());

            valid(dataTypes.next(), Maps.immutableEntry(json.getKey(), node));

        }));

        JsonValidatorHelper.checkIfCondition(!dataTypes.hasNext(), "MISSING", json.getKey());
    }

    private void validDefaultType(DataType dataType, Map.Entry<String, JsonNode> json) throws JsonValidatorException {

        jsonValidatorHelper.valid(dataType, json);
    }

    private DataType toDataType(String key, String table) throws JsonValidatorException {

        TableMetadata tableMetadata = MetadataSchemaUtils.getTableMetadata(session, table);
        Optional<ColumnMetadata> column = tableMetadata.getColumn(key);

        if (!column.isPresent()) {
            throw new JsonValidatorException(UNKNOWN_EXCEPTION, key);
        }

        return column.get().getType();
    }

}
